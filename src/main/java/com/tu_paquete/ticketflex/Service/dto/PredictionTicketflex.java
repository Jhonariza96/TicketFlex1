package com.tu_paquete.ticketflex.Service.dto;

public class PredictionTicketflex {
    private final String prediction;
    private final String confidence;
    private final double confidenceValue;

    public PredictionTicketflex(String prediction, String confidence, double confidenceValue) {
        this.prediction = prediction;
        this.confidence = confidence;
        this.confidenceValue = confidenceValue;
    }

    public String getPrediction() {
        return prediction;
    }

    public String getConfidence() {
        return confidence;
    }

    public double getConfidenceValue() {
        return confidenceValue;
    }

    public double getConfidencePercentage() {
        return confidenceValue;
    }
}