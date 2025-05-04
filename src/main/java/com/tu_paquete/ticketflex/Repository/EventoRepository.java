package com.tu_paquete.ticketflex.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tu_paquete.ticketflex.Model.Evento;
import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.dto.EventoConEstadisticas;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventoRepository extends JpaRepository<Evento, Integer> {

    List<Evento> findByLugar(String lugar);

    List<Evento> findByFecha(LocalDate fecha);

    List<Evento> findByCategoria(String categoria);

    List<Evento> findByArtista(String artista);

    // También puedes agregar un método con combinación de filtros si es necesario
    List<Evento> findByLugarAndFechaAndCategoriaAndArtista(String lugar, LocalDate fecha, String categoria,
            String artista);

    // Opción 1: Usando el nombre exacto del campo
    List<Evento> findByCreador_IdUsuario(Integer idUsuario);

    // Versión paginada
    Page<Evento> findByCreador_IdUsuario(Integer idUsuario, Pageable pageable);

    @Query("SELECT new com.tu_paquete.ticketflex.dto.EventoConEstadisticas(" +
            "e.idEvento, e.nombreEvento, e.fecha, " +
            "COUNT(b.idBoleto), e.disponibilidad, " +
            "SUM(b.precioTotal)) " +
            "FROM Evento e LEFT JOIN Boleto b ON b.evento.idEvento = e.idEvento " +
            "WHERE e.creador.idUsuario = :idCreador " +
            "GROUP BY e.idEvento")
    List<EventoConEstadisticas> findEventosConEstadisticas(@Param("idCreador") Integer idCreador);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Evento e WHERE e.idEvento = :id")
    Optional<Evento> findByIdWithLock(@Param("id") Integer id);

}
