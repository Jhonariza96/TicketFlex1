package com.tu_paquete.ticketflex.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tu_paquete.ticketflex.Model.TicketflexPrediccion;

@Repository
public interface TicketflexPrediccionRepository extends JpaRepository<TicketflexPrediccion, Long> {
    Optional<TicketflexPrediccion> findByIdCliente(Long idCliente);

}