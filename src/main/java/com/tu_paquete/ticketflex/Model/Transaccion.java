package com.tu_paquete.ticketflex.Model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "transacciones")
public class Transaccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaccion")
    private Integer idTransaccion;
    
    @Column(name = "id_boleta")
    private Integer idBoleta; // O lo que consideres necesario para la boleta
    
    @Column(name = "id_usuario")
    private Integer idUsuario;
    
    @Column(name = "id_evento")
    private Integer idEvento;

    @Column(name = "cantidad_boletos")
    private Integer cantidadBoletos;


    private BigDecimal total; // Asegúrate de importar java.math.BigDecimal
    
    @Column(name = "numero_cuotas")
    private Integer numero_Cuotas;
    
    @Column(name = "estado_Pago")
    private String estadoPago; // Por ejemplo: PENDIENTE, EXITOSO, etc.


    @Column(name = "fecha_pago")
    private Timestamp fechaPago;
    
    // Constructor por defecto
    public Transaccion() {}

    // Getters y Setters
    public Integer getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(Integer idTransaccion) {
        this.idTransaccion = idTransaccion;
    }

    public Integer getIdBoleta() {
        return idBoleta;
    }

    public void setIdBoleta(Integer idBoleta) {
        this.idBoleta = idBoleta;
    }

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

    public Integer getCantidadBoletos() {
        return cantidadBoletos;
    }

    public void setCantidadBoletos(Integer cantidadBoletos) {
        this.cantidadBoletos = cantidadBoletos;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

   

    public Integer getNumero_Cuotas() {
		return numero_Cuotas;
	}

	public void setNumero_Cuotas(Integer numero_Cuotas) {
		this.numero_Cuotas = numero_Cuotas;
	}

	public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }
    public Timestamp getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(Timestamp fechaPago) {
        this.fechaPago = fechaPago;
    }
}
