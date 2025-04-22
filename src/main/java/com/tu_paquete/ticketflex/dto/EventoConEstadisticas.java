package com.tu_paquete.ticketflex.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EventoConEstadisticas {
    private Integer idEvento;
    private String nombreEvento;
    private LocalDate fecha;
    private Long boletosVendidos;
    private Integer capacidad;
    private BigDecimal ingresos;
    
    // Constructor
    public EventoConEstadisticas(Integer idEvento, String nombreEvento, LocalDate fecha, 
                                Long boletosVendidos, Integer capacidad, BigDecimal ingresos) {
        this.idEvento = idEvento;
        this.nombreEvento = nombreEvento;
        this.fecha = fecha;
        this.boletosVendidos = boletosVendidos != null ? boletosVendidos : 0L;
        this.capacidad = capacidad;
        this.ingresos = ingresos != null ? ingresos : BigDecimal.ZERO;
    }
    
    // Calcula el porcentaje de ocupación
    public Integer getPorcentajeOcupacion() {
        if (capacidad == null || capacidad == 0 || boletosVendidos == null) {
            return 0;
        }
        return (int) Math.round((boletosVendidos.doubleValue() / capacidad) * 100);
    }
    
    // Getters
    public Integer getIdEvento() { return idEvento; }
    public String getNombreEvento() { return nombreEvento; }
    public LocalDate getFecha() { return fecha; }
    public Long getBoletosVendidos() { return boletosVendidos; }
    public Integer getCapacidad() { return capacidad; }
    public BigDecimal getIngresos() { return ingresos; }
}