package com.tu_paquete.ticketflex.Model;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrediccionResponse {
    private Long idCliente;
    private String genero;
    private int edad;
    private String generoFavorito;
    private double gastoPromedio;
    private String prediccion;
    private double probabilidad;
}