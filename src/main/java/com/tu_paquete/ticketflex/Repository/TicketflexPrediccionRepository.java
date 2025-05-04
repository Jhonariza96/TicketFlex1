package com.tu_paquete.ticketflex.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tu_paquete.ticketflex.Model.TicketflexPrediccion;

@Repository
public interface TicketflexPrediccionRepository extends JpaRepository<TicketflexPrediccion, Long> {

    @Query("""
            SELECT
                t.idCliente as idCliente,
                t.genero as genero,
                t.edad as edad,
                t.generoFavorito as generoFavorito,
                t.promedioGastoHistorico as gastoPromedio,
                t.totalEventosAsistidos as totalEventosAsistidos
            FROM TicketflexPrediccion t
            """)
    Page<ClienteProjection> findAllClientesForPredictionJpql(Pageable pageable);

    interface ClienteProjection {
        Long getIdCliente();

        String getGenero();

        Integer getEdad();

        String getGeneroFavorito();

        Double getGastoPromedio();

        Integer getTotalEventosAsistidos();
    }
}