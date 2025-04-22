package com.tu_paquete.ticketflex.Repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tu_paquete.ticketflex.Model.Boleto;


public interface BoletoRepository extends JpaRepository<Boleto, Long>{
	@Query(value = "SELECT COUNT(*) FROM boleto b " +
            "JOIN eventos e ON b.id_evento = e.id_evento " +
            "WHERE e.id_usuario = :idCreador AND b.estado = :estado", nativeQuery = true)Long countBoletosVendidosPorCreador(
            		@Param("idCreador") Integer idCreador,
            		@Param("estado") String estado);
	@Query("SELECT COUNT(b) FROM Boleto b WHERE b.estado = :estado")
    Long countByEstado(@Param("estado") Boleto.EstadoBoleto estado);
    
    @Query("SELECT SUM(b.precioTotal) FROM Boleto b WHERE b.estado = :estado")
    BigDecimal sumIngresosByEstado(@Param("estado") Boleto.EstadoBoleto estado);
}



