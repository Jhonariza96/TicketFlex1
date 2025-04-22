package com.tu_paquete.ticketflex.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tu_paquete.ticketflex.PagoRequest;
import com.tu_paquete.ticketflex.Model.Boleto;
import com.tu_paquete.ticketflex.Model.Evento;
import com.tu_paquete.ticketflex.Model.Transaccion;
import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.Repository.BoletoRepository;
import com.tu_paquete.ticketflex.Repository.EventoRepository;
import com.tu_paquete.ticketflex.Repository.UsuarioRepository;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;

@Service
public class BoletoService {
    @Autowired
    private BoletoRepository boletoRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TransaccionService transaccionService;

    @Transactional
    public Boleto comprarBoleto(Integer idEvento, Integer idUsuario, Integer cantidad) {
        // Validaciones iniciales
        if (cantidad <= 0 || cantidad > 5) {
            throw new RuntimeException("La cantidad debe ser entre 1 y 5");
        }

        Evento evento = eventoRepository.findById(idEvento)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar disponibilidad con bloqueo pesimista
        evento = eventoRepository.findByIdWithLock(idEvento)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        
        if (evento.getDisponibilidad() < cantidad) {
            throw new RuntimeException("No hay suficiente disponibilidad");
        }

        // Crear boleto
        Boleto boleto = new Boleto();
        boleto.setEvento(evento);
        boleto.setUsuario(usuario);
        boleto.setFechaCompra(new Date());
        boleto.setCantidad(cantidad);
        boleto.setPrecio(evento.getPrecioBase());
        boleto.setPrecioTotal(evento.getPrecioBase().multiply(BigDecimal.valueOf(cantidad)));
        boleto.setEstado(Boleto.EstadoBoleto.PENDIENTE); // Estado inicial como PENDIENTE
        boleto.setFechaLimitePago(LocalDate.now().plusDays(3));

        // Actualizar disponibilidad
        evento.setDisponibilidad(evento.getDisponibilidad() - cantidad);
        eventoRepository.save(evento);

        // Guardar boleto
        Boleto boletoGuardado = boletoRepository.save(boleto);

        // Crear transacción (pero no marcarla como completada aún)
        Transaccion transaccion = new Transaccion();
        transaccion.setIdBoleta(boletoGuardado.getIdBoleto());
        transaccion.setIdUsuario(usuario.getIdUsuario());
        transaccion.setIdEvento(evento.getIdEvento());
        transaccion.setCantidadBoletos(cantidad);
        transaccion.setTotal(boletoGuardado.getPrecioTotal());
        transaccion.setFechaPago(new Timestamp(System.currentTimeMillis()));
        transaccion.setEstadoPago("pendiente"); // Estado inicial
        
        transaccionService.crearTransaccion(transaccion);

        return boletoGuardado;
    }
    
 // Método para procesar el pago
    public boolean procesarPago(PagoRequest pagoRequest) {
        // Aquí podrías realizar la integración con un sistema de pago, por ejemplo PayU, MercadoPago, etc.
        // Este es un ejemplo básico sin integración real con un servicio de pago.

        // Ejemplo de validación simple (solo para fines demostrativos)
        if (pagoRequest.getBanco() == null || pagoRequest.getNumeroTarjeta().isEmpty() || 
            pagoRequest.getFechaVencimiento().isEmpty() || pagoRequest.getCvv().isEmpty()) {
            return false;  // Si falta algún dato, el pago falla.
        }

        // Simula el pago
        System.out.println("Procesando pago de " + pagoRequest.getCantidad() + " boletos para el evento " +
                pagoRequest.getIdEvento() + " del usuario " + pagoRequest.getIdUsuario() + " a través de " +
                pagoRequest.getBanco());

        // Aquí va la lógica de integración real con el sistema de pago.
        // Podrías conectarte a un API de pagos, validar la tarjeta, verificar fondos, etc.

        // Si todo va bien, simula que el pago fue exitoso
        return true;
    }
    
}


