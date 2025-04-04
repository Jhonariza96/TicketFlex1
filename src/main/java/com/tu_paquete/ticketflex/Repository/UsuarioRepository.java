package com.tu_paquete.ticketflex.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tu_paquete.ticketflex.Model.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email); // Devuelve un Optional<Usuario>
    
 // Añade este método para verificación más eficiente
    boolean existsByEmail(String email);
    
    // Método nativo para verificación directa
    @Query(value = "SELECT COUNT(*) > 0 FROM usuarios WHERE email = ?1", nativeQuery = true)
    boolean existsByEmailNative(String email);
}