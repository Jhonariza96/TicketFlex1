package com.tu_paquete.ticketflex.Service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.tu_paquete.ticketflex.Model.TicketflexPrediccion;
import com.tu_paquete.ticketflex.Repository.TicketflexPrediccionRepository;
import com.tu_paquete.ticketflex.Service.dto.PredictionTicketflex;

import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Attribute;
import weka.core.converters.ConverterUtils.DataSource;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class TicketflexService {

    private static final Logger LOGGER = Logger.getLogger(TicketflexService.class.getName());
    private Classifier classifier;
    private Instances dataStructure;
    private final TicketflexPrediccionRepository repository;

    public TicketflexService(TicketflexPrediccionRepository repository) {
        this.repository = repository;
        loadModelAndDataStructure();
    }

    private void loadModelAndDataStructure() {
        try {
            ClassPathResource modelResource = new ClassPathResource("ticketflex_dataset_balanceado.model");
            classifier = (Classifier) weka.core.SerializationHelper.read(modelResource.getInputStream());

            ClassPathResource arffResource = new ClassPathResource("ticketflex_dataset_balanceado.arff");
            DataSource source = new DataSource(arffResource.getInputStream());
            dataStructure = source.getDataSet();
            dataStructure.setClassIndex(dataStructure.numAttributes() - 1);

            logDataStructure();

        } catch (Exception e) {
            LOGGER.severe("Error al inicializar TicketflexService: " + e.getMessage());
            throw new RuntimeException("No se pudo inicializar el servicio de predicción", e);
        }
    }

    private void logDataStructure() {
        LOGGER.info("=== ESTRUCTURA DE DATOS ===");
        for (int i = 0; i < dataStructure.numAttributes(); i++) {
            Attribute attr = dataStructure.attribute(i);
            LOGGER.info(String.format("Atributo %d: %s (%s)",
                    i, attr.name(), attr.isNominal() ? "Nominal" : "Numeric"));
            if (attr.isNominal()) {
                LOGGER.info("Valores permitidos: " + attr.toString());
            }
        }
    }

    public PredictionTicketflex predictAndSave(TicketflexPrediccion datos) throws Exception {
        LOGGER.info("Datos recibidos: " + datos.toString());

        if (datos.getGenero() == null || datos.getEdad() == null ||
                datos.getTotalEventosAsistidos() == null || datos.getGeneroFavorito() == null ||
                datos.getPromedioGastoHistorico() == null || datos.getNuevoGeneroConcierto() == null ) {

            throw new IllegalArgumentException("Faltan datos obligatorios para la predicción.");
        }

        try {
            Instance instance = createWekaInstance(datos);

            double predictionValue = classifier.classifyInstance(instance);
            String prediction = dataStructure.classAttribute().value((int) predictionValue);

            double[] probabilities = classifier.distributionForInstance(instance);
            double confidence = probabilities[(int) predictionValue] * 100;

            String confidencePercentage = new DecimalFormat("#.#").format(confidence) + "%";

            datos.setResultadoPrediccion(prediction);
            datos.setConfianza(confidence);

            repository.save(datos);

            LOGGER.info("Predicción guardada con ID: " + datos.getId());

            return new PredictionTicketflex(prediction, confidencePercentage, confidence);

        } catch (Exception e) {
            LOGGER.severe("Error en predicción: " + e.getMessage());
            throw new Exception("Error al procesar la predicción: " + e.getMessage(), e);
        }
    }

    private Instance createWekaInstance(TicketflexPrediccion datos) throws Exception {
        Instance instance = new DenseInstance(dataStructure.numAttributes());
        instance.setDataset(dataStructure);

        // Mapeo exacto de los campos del dataset ARFF
        setValue(instance, "Genero", datos.getGenero());
        setValue(instance, "Edad", datos.getEdad());
        setValue(instance, "Total_Eventos_Asistidos", datos.getTotalEventosAsistidos());
        setValue(instance, "Genero_Favorito", datos.getGeneroFavorito());
        setValue(instance, "Promedio_Gasto_Historico", datos.getPromedioGastoHistorico());
        setValue(instance, "Nuevo_Genero_Concierto", datos.getNuevoGeneroConcierto());
        setValue(instance, "Nuevo_Precio", datos.getNuevoPrecio());
        setValue(instance, "Descuento_Disponible", datos.isDescuentoDisponible() ? "1" : "0");

        return instance;
    }

    private void setValue(Instance instance, String attrName, Object value) throws Exception {
        Attribute attr = dataStructure.attribute(attrName);
        if (attr == null) {
            throw new Exception("Atributo '" + attrName + "' no encontrado");
        }

        if (attr.isNominal()) {
            setNominalValue(instance, attr, value);
        } else {
            setNumericValue(instance, attr, value);
        }
    }

    private void setNominalValue(Instance instance, Attribute attr, Object value) throws Exception {
        String stringValue = value.toString();
        int index = attr.indexOfValue(stringValue);
        if (index == -1) {
            throw new Exception("Valor '" + stringValue + "' no válido para " + attr.name() +
                    ". Valores permitidos: " + attr.toString());
        }
        instance.setValue(attr, index);
    }

    private void setNumericValue(Instance instance, Attribute attr, Object value) throws Exception {
        if (value instanceof Number) {
            instance.setValue(attr, ((Number) value).doubleValue());
        } else {
            throw new Exception("Valor numérico esperado para " + attr.name());
        }
    }

    public void logError(String message, Exception e) {
        LOGGER.severe(message + ": " + e.getMessage());
    }

    public Optional<TicketflexPrediccion> buscarPorIdCliente(Long idCliente) {
        return repository.findByIdCliente(idCliente);
    }

}
