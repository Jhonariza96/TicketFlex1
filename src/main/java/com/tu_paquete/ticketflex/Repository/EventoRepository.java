package com.tu_paquete.ticketflex.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tu_paquete.ticketflex.Model.Evento;
import com.tu_paquete.ticketflex.Model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventoRepository extends JpaRepository<Evento, Integer> {
 
    List<Evento> findByLugar(String lugar);
    List<Evento> findByFecha(LocalDate fecha);
    List<Evento> findByCategoria(String categoria);
    List<Evento> findByArtista(String artista);

    // También puedes agregar un método con combinación de filtros si es necesario
    List<Evento> findByLugarAndFechaAndCategoriaAndArtista(String lugar, LocalDate fecha, String categoria, String artista);
    
    
 // Opción 1: Usando el nombre exacto del campo
    List<Evento> findByCreador_IdUsuario(Integer idUsuario);
    
    // Versión paginada
    Page<Evento> findByCreador_IdUsuario(Integer idUsuario, Pageable pageable);

}

