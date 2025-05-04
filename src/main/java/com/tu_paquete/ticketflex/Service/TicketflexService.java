package com.tu_paquete.ticketflex.Service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tu_paquete.ticketflex.Model.TicketflexPrediccion;
import com.tu_paquete.ticketflex.Repository.TicketflexPrediccionRepository;
import com.tu_paquete.ticketflex.Repository.TicketflexPrediccionRepository.ClienteProjection;
import com.tu_paquete.ticketflex.Service.dto.EventoRequest;
import com.tu_paquete.ticketflex.Service.dto.PrediccionEventoResponse;
import com.tu_paquete.ticketflex.Service.dto.PredictionTicketflex;

import weka.classifiers.Classifier;
import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class TicketflexService {

    private static final Logger LOGGER = Logger.getLogger(TicketflexService.class.getName());
    private static final int BATCH_SIZE = 1000;
    private final Classifier classifier;
    private final Instances dataStructure;
    private final TicketflexPrediccionRepository repository;

    public TicketflexService(TicketflexPrediccionRepository repository) {
        this.repository = repository;
        try {
            // Carga inicial del modelo WEKA
            ClassPathResource modelResource = new ClassPathResource("ticketflex_dataset_balanceado.model");
            this.classifier = (Classifier) weka.core.SerializationHelper.read(modelResource.getInputStream());

            ClassPathResource arffResource = new ClassPathResource("ticketflex_dataset_balanceado.arff");
            DataSource source = new DataSource(arffResource.getInputStream());
            this.dataStructure = source.getDataSet();
            this.dataStructure.setClassIndex(this.dataStructure.numAttributes() - 1);

            logDataStructure();
        } catch (Exception e) {
            LOGGER.severe("Error al inicializar el servicio: " + e.getMessage());
            throw new RuntimeException("Error de inicialización del servicio de predicción", e);
        }
    }

    // ========== MÉTODOS PRINCIPALES ==========
    private static final int MAX_PARALLELISM = Runtime.getRuntime().availableProcessors() * 2;

    @Transactional(readOnly = true)

    public Page<PrediccionEventoResponse> predecirAsistenciaMasivaPaginada(
            EventoRequest eventoRequest, int page, int size, boolean mostrarTodos) {

        validateEventoRequest(eventoRequest);
        Pageable pageable = PageRequest.of(page, size);

        Page<ClienteProjection> clientesPage = repository.findAllClientesForPredictionJpql(pageable);

        List<PrediccionEventoResponse> resultados = clientesPage.getContent()
                .parallelStream()
                .map(cliente -> {
                    try {
                        TicketflexPrediccion datos = buildPredictionData(cliente, eventoRequest);
                        PredictionTicketflex prediction = predict(datos);
                        PrediccionEventoResponse response = buildResponse(cliente, prediction);
                        response.setRazonPrincipal(calcularRazonMejorada(cliente, eventoRequest, prediction));
                        return response;
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error procesando cliente ID: " + cliente.getIdCliente(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(r -> mostrarTodos || "Asistirá".equals(r.getPrediccion()))
                .sorted(Comparator.comparingDouble(PrediccionEventoResponse::getProbabilidad).reversed())
                .collect(Collectors.toList());

        return new PageImpl<>(resultados, pageable, clientesPage.getTotalElements());
    }

    public PredictionTicketflex predictAndSave(TicketflexPrediccion datos) throws Exception {
        validatePredictionData(datos);
        Instance instance = createWekaInstance(datos);

        double predictionValue = classifier.classifyInstance(instance);
        String prediction = dataStructure.classAttribute().value((int) predictionValue);
        double confidence = getPredictionConfidence(instance, predictionValue);

        datos.setResultadoPrediccion(prediction);
        datos.setConfianza(confidence);
        repository.save(datos);

        return new PredictionTicketflex(
                prediction,
                new DecimalFormat("#.#").format(confidence) + "%",
                confidence);
    }

    // ========== MÉTODOS AUXILIARES ==========

    private List<PrediccionEventoResponse> processBatch(
            List<ClienteProjection> clientes,
            EventoRequest evento) {

        return clientes.parallelStream()
                .map(cliente -> {
                    try {
                        TicketflexPrediccion datos = buildPredictionData(cliente, evento);
                        // Validar datos antes de predecir
                        validatePredictionData(datos);

                        // Loggear datos para diagnóstico
                        LOGGER.info("Procesando cliente ID: " + cliente.getIdCliente() +
                                " con datos: " + datos.toString());

                        PredictionTicketflex prediction = predict(datos);
                        return buildResponse(cliente, prediction);
                    } catch (Exception e) {
                        LOGGER.warning("Error procesando cliente ID: " + cliente.getIdCliente() +
                                " - Error: " + e.getMessage());
                        LOGGER.warning("Stack trace: " + Arrays.toString(e.getStackTrace()));
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(p -> p.getProbabilidad() > 60.0)
                .collect(Collectors.toList());
    }

    private String calcularRazonMejorada(ClienteProjection cliente, EventoRequest evento,
            PredictionTicketflex prediction) {
        Map<String, Double> factores = new LinkedHashMap<>();

        // Factores base
        factores.put("Género", evento.getGenero().equalsIgnoreCase(cliente.getGeneroFavorito()) ? 0.3 : -0.2);
        factores.put("Precio",
                0.4 * (1 - Math.abs(cliente.getGastoPromedio() - evento.getPrecio()) / evento.getPrecio()));
        factores.put("Frecuencia", Math.min(cliente.getTotalEventosAsistidos() * 0.05, 0.2));
        if (evento.isDescuentoDisponible()) {
            factores.put("Descuento", 0.15);
        }

        // Seleccionar top 3 factores
        return factores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .map(e -> e.getKey() + " (" + (e.getValue() > 0 ? "+" : "") +
                        new DecimalFormat("#%").format(e.getValue()) + ")")
                .collect(Collectors.joining(", "));
    }

    private TicketflexPrediccion buildPredictionData(ClienteProjection cliente, EventoRequest evento) {
        TicketflexPrediccion datos = new TicketflexPrediccion();
        datos.setIdCliente(cliente.getIdCliente());
        datos.setGenero(Optional.ofNullable(cliente.getGenero()).orElse("Otro"));
        datos.setEdad(Optional.ofNullable(cliente.getEdad()).orElse(30));
        datos.setGeneroFavorito(cliente.getGeneroFavorito());
        datos.setPromedioGastoHistorico(Optional.ofNullable(cliente.getGastoPromedio()).orElse(100.0));
        datos.setTotalEventosAsistidos(Optional.ofNullable(cliente.getTotalEventosAsistidos()).orElse(1));
        datos.setNuevoGeneroConcierto(evento.getGenero());
        datos.setNuevoPrecio(evento.getPrecio());
        datos.setDescuentoDisponible(evento.isDescuentoDisponible());
        return datos;
    }

    private PredictionTicketflex predict(TicketflexPrediccion datos) throws Exception {
        Instance instance = createWekaInstance(datos);
        double predictionValue = classifier.classifyInstance(instance);

        // Obtener la distribución de probabilidades
        double[] distribution = classifier.distributionForInstance(instance);
        double confidence = (distribution[(int) predictionValue] * 0.9 + 0.1) * 100;

        confidence = Math.min(confidence, 95);

        // Interpretación directa (0=No Asistirá, 1=Asistirá)
        String predictionText = predictionValue == 1.0 ? "Asistirá" : "No Asistirá";

        if (predictionText.equals("Asistirá")) {
            double ajuste = calcularAjusteContextual(datos);
            confidence = confidence * ajuste;
        }
        return new PredictionTicketflex(
                predictionText, // Devuelve el texto directamente
                new DecimalFormat("#.#").format(confidence) + "%",
                confidence);
    }

    private double calcularAjusteContextual(TicketflexPrediccion datos) {
        double ajuste = 1.0;

        // Ajuste por diferencia de precio
        double ratioPrecio = datos.getPromedioGastoHistorico() / datos.getNuevoPrecio();
        if (ratioPrecio < 0.7)
            ajuste *= 0.8;
        if (ratioPrecio > 1.5)
            ajuste *= 1.1;

        // Ajuste por frecuencia
        if (datos.getTotalEventosAsistidos() > 5)
            ajuste *= 1.05;

        return Math.min(ajuste, 1.0);
    }

    private double getPredictionConfidence(Instance instance, double predictionValue) throws Exception {
        double[] distribution = classifier.distributionForInstance(instance);
        return distribution[(int) predictionValue] * 100;
    }

    private PrediccionEventoResponse buildResponse(ClienteProjection cliente, PredictionTicketflex prediction) {
        PrediccionEventoResponse response = new PrediccionEventoResponse();
        response.setIdCliente(cliente.getIdCliente());
        response.setGenero(cliente.getGenero());
        response.setEdad(cliente.getEdad());
        response.setPrediccion(prediction.getPrediction());
        response.setProbabilidad(prediction.getConfidenceValue());
        response.setGeneroFavorito(cliente.getGeneroFavorito());
        response.setGastoPromedio(cliente.getGastoPromedio());
        // La razón principal se establecerá después con setRazonPrincipal()
        return response;
    }

    // ========== VALIDACIONES ==========

    private void validateEventoRequest(EventoRequest request) {
        if (request.getGenero() == null || request.getGenero().trim().isEmpty()) {
            throw new IllegalArgumentException("El género del evento es requerido");
        }
        if (request.getPrecio() == null || request.getPrecio() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
    }

    private void validatePredictionData(TicketflexPrediccion datos) {
        Objects.requireNonNull(datos, "Los datos de predicción no pueden ser nulos");

        if (datos.getGenero() == null) {
            throw new IllegalArgumentException("El género del cliente es requerido");
        }
        if (datos.getEdad() == null || datos.getEdad() <= 0) {
            throw new IllegalArgumentException("La edad debe ser mayor a 0");
        }
        if (datos.getTotalEventosAsistidos() == null || datos.getTotalEventosAsistidos() < 0) {
            throw new IllegalArgumentException("Total eventos asistidos no puede ser negativo");
        }
        if (datos.getGeneroFavorito() == null || datos.getGeneroFavorito().trim().isEmpty()) {
            throw new IllegalArgumentException("Género favorito es requerido");
        }
        if (datos.getPromedioGastoHistorico() == null || datos.getPromedioGastoHistorico() < 0) {
            throw new IllegalArgumentException("Gasto promedio no puede ser negativo");
        }
        if (datos.getNuevoGeneroConcierto() == null || datos.getNuevoGeneroConcierto().trim().isEmpty()) {
            throw new IllegalArgumentException("Género del concierto es requerido");
        }
        if (datos.getNuevoPrecio() == null || datos.getNuevoPrecio() <= 0) {
            throw new IllegalArgumentException("Precio debe ser mayor a 0");
        }
    }

    // ========== WEKA UTILS ==========

    private Instance createWekaInstance(TicketflexPrediccion datos) throws Exception {
        Instance instance = new DenseInstance(dataStructure.numAttributes());
        instance.setDataset(dataStructure);

        Map<String, Object> fieldMap = new ConcurrentHashMap<>();
        fieldMap.put("Genero", datos.getGenero());
        fieldMap.put("Edad", datos.getEdad());
        fieldMap.put("Total_Eventos_Asistidos", datos.getTotalEventosAsistidos());
        fieldMap.put("Genero_Favorito", datos.getGeneroFavorito());
        fieldMap.put("Promedio_Gasto_Historico", datos.getPromedioGastoHistorico());
        fieldMap.put("Nuevo_Genero_Concierto", datos.getNuevoGeneroConcierto());
        fieldMap.put("Nuevo_Precio", datos.getNuevoPrecio());
        fieldMap.put("Descuento_Disponible", datos.isDescuentoDisponible() ? "1" : "0");

        fieldMap.forEach((fieldName, value) -> {
            try {
                setInstanceValue(instance, fieldName, value);
            } catch (Exception e) {
                LOGGER.warning("Error setting field " + fieldName + ": " + e.getMessage());
            }
        });

        return instance;
    }

    private void setInstanceValue(Instance instance, String attrName, Object value) throws Exception {
        Attribute attr = dataStructure.attribute(attrName);
        if (attr == null)
            throw new Exception("Atributo '" + attrName + "' no encontrado");

        if (attr.isNominal()) {
            String stringValue = value.toString();
            int index = attr.indexOfValue(stringValue);
            if (index == -1) {
                throw new Exception("Valor no válido para " + attr.name() + ": " + stringValue);
            }
            instance.setValue(attr, index);
        } else {
            if (value instanceof Number) {
                instance.setValue(attr, ((Number) value).doubleValue());
            } else {
                throw new Exception("Valor numérico esperado para " + attr.name());
            }
        }
    }

    // Agrega esto en el constructor del servicio:
    private void logDataStructure() {
        LOGGER.info("=== WEKA DATA STRUCTURE ===");
        LOGGER.info("Attributes:");
        for (int i = 0; i < dataStructure.numAttributes(); i++) {
            Attribute attr = dataStructure.attribute(i);
            LOGGER.info(i + ": " + attr.name() + " (" + attr.type() + ")");
        }
        LOGGER.info("Class attribute: " + dataStructure.classAttribute().name());
    }
}