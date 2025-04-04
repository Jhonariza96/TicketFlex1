package com.tu_paquete.ticketflex;

import java.math.BigDecimal;

public class PagoRequest {
    private Integer idUsuario;
    private Integer idEvento;
    private Integer cantidad;
    private Integer idGraderia; // Nuevo campo para gradería
    
    // Información de pago
    private MetodoPago metodoPago; // Enum para tipos de pago
    private String banco;
    private String numeroTarjeta;
    private String fechaVencimiento;
    private String cvv;
    private Integer numeroCuotas; // Solo para tarjeta crédito
    
    // Campos calculados (opcional)
    private BigDecimal precioUnitario;
    private BigDecimal total;

    public enum MetodoPago {
        TARJETA_DEBITO,
        TARJETA_CREDITO,
        PAYPAL,
        TRANSFERENCIA
    }

    // Getters y setters
    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(Integer idEvento) {
        this.idEvento = idEvento;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getIdGraderia() {
        return idGraderia;
    }

    public void setIdGraderia(Integer idGraderia) {
        this.idGraderia = idGraderia;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getBanco() {
        return banco;
    }

    public void setBanco(String banco) {
        this.banco = banco;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public Integer getNumeroCuotas() {
        return numeroCuotas;
    }

    public void setNumeroCuotas(Integer numeroCuotas) {
        this.numeroCuotas = numeroCuotas;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    // Método de validación
    public boolean isValid() {
        if (metodoPago == null) return false;
        
        // Validación para pagos con tarjeta
        if (metodoPago == MetodoPago.TARJETA_DEBITO || metodoPago == MetodoPago.TARJETA_CREDITO) {
            if (banco == null || banco.isEmpty()) return false;
            if (numeroTarjeta == null || numeroTarjeta.isEmpty()) return false;
            if (fechaVencimiento == null || fechaVencimiento.isEmpty()) return false;
            if (cvv == null || cvv.isEmpty()) return false;
            
            // Validación especial para tarjeta crédito
            if (metodoPago == MetodoPago.TARJETA_CREDITO && (numeroCuotas == null || numeroCuotas < 1)) {
                return false;
            }
        }
        
        return idUsuario != null && idEvento != null && cantidad != null && cantidad > 0;
    }
}
