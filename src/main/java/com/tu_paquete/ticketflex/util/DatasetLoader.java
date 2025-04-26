package com.tu_paquete.ticketflex.util;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.tu_paquete.ticketflex.Model.TicketflexPrediccion;
import com.tu_paquete.ticketflex.Repository.TicketflexPrediccionRepository;

import java.util.ArrayList;
import java.util.List;

@Component
public class DatasetLoader implements CommandLineRunner {

    @Autowired
    private TicketflexPrediccionRepository ticketflexPrediccionRepository;

    @Override
    public void run(String... args) throws Exception {
        // Solo carga si no hay datos en la base
        if (ticketflexPrediccionRepository.count() > 0) {
            System.out.println("⚠️ Ya hay datos en la base. No se volvió a cargar el dataset.");
            return;
        }

        // Ruta al archivo ARFF
        DataSource source = new DataSource("src/main/resources/ticketflex_dataset_balanceado.arff");
        Instances data = source.getDataSet();

        // Establece la clase si no está definida
        if (data.classIndex() == -1)
            data.setClassIndex(data.numAttributes() - 1);

        List<TicketflexPrediccion> registros = new ArrayList<>();

        for (int i = 0; i < data.numInstances(); i++) {
            var instancia = data.instance(i);
            var pred = new TicketflexPrediccion();
        
            pred.setIdCliente((long) instancia.value(data.attribute("ID_Cliente")));
            pred.setGenero(instancia.stringValue(data.attribute("Genero")));
            pred.setEdad((int) instancia.value(data.attribute("Edad")));
            pred.setTotalEventosAsistidos((int) instancia.value(data.attribute("Total_Eventos_Asistidos")));
            pred.setGeneroFavorito(instancia.stringValue(data.attribute("Genero_Favorito")));
            pred.setPromedioGastoHistorico(instancia.value(data.attribute("Promedio_Gasto_Historico")));
            pred.setNuevoGeneroConcierto(instancia.stringValue(data.attribute("Nuevo_Genero_Concierto")));
            pred.setNuevoPrecio(instancia.value(data.attribute("Nuevo_Precio")));
        
            // Manejo correcto para 0 y 1 como booleano
            String descuentoStr = instancia.stringValue(data.attribute("Descuento_Disponible"));
            pred.setDescuentoDisponible(descuentoStr.equals("1"));
        
            pred.setResultadoPrediccion(instancia.stringValue(data.attribute("Asistira")));
            pred.setConfianza(100.0); // Valor fijo para los datos iniciales
        
            registros.add(pred);
        }
        
        ticketflexPrediccionRepository.saveAll(registros);
        System.out.println("✅ Dataset cargado en la base de datos.");
    }
}
