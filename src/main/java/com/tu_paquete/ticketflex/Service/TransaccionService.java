package com.tu_paquete.ticketflex.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tu_paquete.ticketflex.Model.Transaccion;
import com.tu_paquete.ticketflex.Repository.TransaccionRepository;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;


@Service
public class TransaccionService {
    @Autowired
    private TransaccionRepository transaccionRepository;

    @Transactional
    public Transaccion crearTransaccion(Transaccion transaccion) {
        // Validaciones adicionales si son necesarias
        if (transaccion.getTotal() == null || transaccion.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El total de la transacción debe ser mayor que cero");
        }
        
        return transaccionRepository.save(transaccion);
    }

    public List<Transaccion> listarTransacciones() {
        return transaccionRepository.findAll();
    }

    public Transaccion obtenerTransaccionPorId(Integer id) {
        return transaccionRepository.findById(id).orElse(null);
    }

    public void eliminarTransaccion(Integer id) {
        transaccionRepository.deleteById(id);
    }
    
    public List<Transaccion> obtenerHistorialDeCompras(Integer idUsuario) {
        // Buscar todas las transacciones de un usuario específico
        return transaccionRepository.findByIdUsuario(idUsuario);
    }
}








