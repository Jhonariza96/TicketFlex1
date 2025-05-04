package com.tu_paquete.ticketflex.Service.dto;

public class EventoRequest {
    private String genero;
    private Double precio;
    private boolean descuentoDisponible;

    // Getters y Setters
    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public boolean isDescuentoDisponible() {
        return descuentoDisponible;
    }

    public void setDescuentoDisponible(boolean descuentoDisponible) {
        this.descuentoDisponible = descuentoDisponible;
    }
}