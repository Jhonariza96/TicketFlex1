/*package com.tu_paquete.ticketflex.util;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class ModelGenerator {
    public static void main(String[] args) {
        try {
            System.out.println("Cargando dataset...");
            DataSource source = new DataSource("src/main/resources/compra_boleta_dataset.arff");
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);
            
            System.out.println("Entrenando modelo J48...");
            Classifier classifier = new J48();
            classifier.buildClassifier(data);
            
            System.out.println("Guardando modelo...");
            weka.core.SerializationHelper.write("src/main/resources/compra_boleta_dataset.model", classifier);
            
            System.out.println("Modelo generado exitosamente!");
            System.out.println("Ubicación: src/main/resources/compra_boleta_dataset.model");
        } catch (Exception e) {
            System.err.println("Error generando modelo:");
            e.printStackTrace();
        }
    }
}*/