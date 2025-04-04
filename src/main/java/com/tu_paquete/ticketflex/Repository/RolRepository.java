package com.tu_paquete.ticketflex.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tu_paquete.ticketflex.Model.Rol;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    // Busca por nombre_rol (coincide con el campo en la BD)
    Optional<Rol> findByNombreRol(String nombreRol);
}