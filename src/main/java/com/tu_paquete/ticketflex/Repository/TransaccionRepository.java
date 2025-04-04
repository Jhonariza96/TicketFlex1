package com.tu_paquete.ticketflex.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tu_paquete.ticketflex.Model.Transaccion;

public interface TransaccionRepository extends JpaRepository<Transaccion, Integer> {
	List<Transaccion> findByIdUsuario(Integer idUsuario);
}
