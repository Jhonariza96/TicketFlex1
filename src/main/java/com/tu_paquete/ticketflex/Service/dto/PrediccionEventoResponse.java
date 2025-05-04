package com.tu_paquete.ticketflex.Service.dto;

public class PrediccionEventoResponse {
    private Long idCliente;
    private String genero;
    private Integer edad;
    private String prediccion;
    private Double probabilidad;
    private String razonPrincipal;
    private String generoFavorito;
    private Double gastoPromedio;

    // Constructor vacío
    public PrediccionEventoResponse() {}

    // Getters y Setters
    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getPrediccion() {
        return prediccion;
    }

    public void setPrediccion(String prediccion) {
        this.prediccion = prediccion;
    }

    public Double getProbabilidad() {
        return probabilidad;
    }

    public void setProbabilidad(Double probabilidad) {
        this.probabilidad = probabilidad;
    }

    public String getRazonPrincipal() {
        return razonPrincipal;
    }

    public void setRazonPrincipal(String razonPrincipal) {
        this.razonPrincipal = razonPrincipal;
    }

    public String getGeneroFavorito() {
        return generoFavorito;
    }

    public void setGeneroFavorito(String generoFavorito) {
        this.generoFavorito = generoFavorito;
    }

    public Double getGastoPromedio() {
        return gastoPromedio;
    }

    public void setGastoPromedio(Double gastoPromedio) {
        this.gastoPromedio = gastoPromedio;
    }
}