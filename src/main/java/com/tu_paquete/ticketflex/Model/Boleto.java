package com.tu_paquete.ticketflex.Model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "boleto")
public class Boleto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_boleta")
    private Integer idBoleto;

    @ManyToOne
    @JoinColumn(name = "id_evento")
    private Evento evento;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    private BigDecimal precio;
    private Integer cantidad;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCompra;

    private BigDecimal precioTotal;
    
    @Enumerated(EnumType.STRING)
    private EstadoBoleto estado; // PENDIENTE, ACTIVO, CANCELADO

    private LocalDate fechaLimitePago; // Fecha límite para completar el pago
    private String qrCode; // QR temporal/definitivo
    private BigDecimal saldoPendiente; // Saldo restante (opcional)
    
    public enum EstadoBoleto {
        PENDIENTE, ACTIVO, CANCELADO
    }
    

	public Integer getIdBoleto() {
		return idBoleto;
	}

	public void setIdBoleto(Integer idBoleto) {
		this.idBoleto = idBoleto;
	}

	public Evento getEvento() {
		return evento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public BigDecimal getPrecio() {
		return precio;
	}

	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}

	public Integer getCantidad() {
		return cantidad;
	}

	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}

	public Date getFechaCompra() {
		return fechaCompra;
	}

	public void setFechaCompra(Date fechaCompra) {
		this.fechaCompra = fechaCompra;
	}

	public BigDecimal getPrecioTotal() {
		return precioTotal;
	}

	public void setPrecioTotal(BigDecimal precioTotal) {
		this.precioTotal = precioTotal;
	}

	public EstadoBoleto getEstado() {
		return estado;
	}

	public void setEstado(EstadoBoleto estado) {
		this.estado = estado;
	}

	public LocalDate getFechaLimitePago() {
		return fechaLimitePago;
	}

	public void setFechaLimitePago(LocalDate fechaLimitePago) {
		this.fechaLimitePago = fechaLimitePago;
	}

	public String getQrCode() {
		return qrCode;
	}

	public void setQrCode(String qrCode) {
		this.qrCode = qrCode;
	}

	public BigDecimal getSaldoPendiente() {
		return saldoPendiente;
	}

	public void setSaldoPendiente(BigDecimal saldoPendiente) {
		this.saldoPendiente = saldoPendiente;
	}
	
	

    // Getters y Setters
    
}


