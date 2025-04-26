package com.tu_paquete.ticketflex.Model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "ticketflex_predicciones")
public class TicketflexPrediccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(length = 255)
    private String genero;

    @Column(nullable = false)
    private Integer edad;

    @Column(name = "total_eventos_asistidos", nullable = false)
    private Integer totalEventosAsistidos;
    @Column(name = "genero_favorito", length = 255)
    private String generoFavorito;

    @Column(name = "promedio_gasto_historico", nullable = false)
    private Double promedioGastoHistorico;

    @Column(name = "nuevo_genero_concierto", length = 255)
    private String nuevoGeneroConcierto;

    @Column(name = "nuevo_precio", nullable = false)
    private Double nuevoPrecio;

    @Column(name = "descuento_disponible", nullable = false)
    private boolean descuentoDisponible;

    @Column(name = "resultado_prediccion", length = 255)
    private String resultadoPrediccion;

    @Column(columnDefinition = "DECIMAL(5,2)")
    private Double confianza;

    @Column(name = "fecha_prediccion", updatable = false)
    @CreationTimestamp
    private LocalDateTime fechaPrediccion;


}
