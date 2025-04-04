package com.tu_paquete.ticketflex.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.tu_paquete.ticketflex.PagoRequest;
import com.tu_paquete.ticketflex.Model.Boleto;
import com.tu_paquete.ticketflex.Model.Evento;
import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.Service.BoletoService;
import com.tu_paquete.ticketflex.Service.EventoService;
import com.tu_paquete.ticketflex.Service.TransaccionService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/eventos")
public class EventoController {
    @Autowired
    private EventoService eventoService;
    @Autowired
    private TransaccionService transaccionService;
    @Autowired
    private BoletoService boletoService;

    @PostMapping
    public Evento crearEvento(@RequestBody Evento evento, Authentication authentication) {
        // Obtener el usuario autenticado
        Usuario usuarioActual = (Usuario) authentication.getPrincipal();
        System.out.println("ID del creador: " + usuarioActual.getIdUsuario());
        
        // Asignar el usuario autenticado como creador del evento
        evento.setCreador(usuarioActual);
        
        if (evento.getImagen() == null || evento.getImagen().trim().isEmpty()) {
            evento.setImagen("default.jpg");
        }
        
        return eventoService.crearEvento(evento, usuarioActual.getEmail());
    }
    
    @GetMapping("/listar")
    public List<Evento> listarEventos(){
        List<Evento> eventos = eventoService.listarEventos();
        System.out.println("Eventos recuperados: " + eventos);  // Agregar log

        // Recorrer los eventos y asignar imagen predeterminada si es necesario
        for (Evento evento : eventos) {
            if (evento.getImagen() == null || evento.getImagen().trim().isEmpty()) {
                evento.setImagen("default.jpg");
            }
        }

        return eventos;
    }

    
    @GetMapping("/{id}")
    public Evento obtenerEventoPorId(@PathVariable Integer id) {
    	return eventoService.obtenerEventorPorId(id);
    }
    
    @DeleteMapping("/{id}")
    public void eliminarEvento(@PathVariable Integer id) {
    	eventoService.eliminarEvento(id);
    }
    
    // Actualizar un evento
    @PutMapping("/{id}")
    public Evento actualizarEvento(@PathVariable Integer id, @RequestBody Evento eventoActualizado) {
    	Evento eventoExistente = eventoService.obtenerEventorPorId(id);
    	if (eventoExistente != null) {
    		//Actualizamos los campos del evento
    		eventoExistente.setNombreEvento(eventoActualizado.getNombreEvento());
    		eventoExistente.setFecha(eventoActualizado.getFecha());
    		eventoExistente.setLugar(eventoActualizado.getLugar());
    		eventoExistente.setPrecioBase(eventoActualizado.getPrecioBase());
    		
    		 if (eventoActualizado.getImagen() == null || eventoActualizado.getImagen().trim().isEmpty()) {
    	            eventoExistente.setImagen("default.jpg"); // Imagen predeterminada
    	        } else {
    	            eventoExistente.setImagen(eventoActualizado.getImagen()); // Mantener la imagen si no es null
    	        }
    		return eventoService.actualizarEvento(eventoExistente);
    	}
    	return null;
    }
    
    @PostMapping("/{id_evento}/comprar")
    public ResponseEntity<String> comprarBoleto(@PathVariable Integer id_evento, @RequestParam Integer cantidad, @RequestParam Integer idUsuario) {
        if (cantidad <= 0 || cantidad > 5) {
            return ResponseEntity.badRequest().body("La cantidad debe ser mayor o igual a 1 y menor o igual a 5.");
        }

        // Lógica para procesar la compra
        try {
            Boleto boleto = boletoService.comprarBoleto(id_evento, idUsuario, cantidad);
            return ResponseEntity.ok("Compra exitosa, boletos comprados: " + cantidad);
        }catch (RuntimeException e) {
            return ResponseEntity.status(500).body(e.getMessage());
            }
        }
    // Endpoint para filtrar los eventos
    @PostMapping("/filtrar")
    public List<Evento> filtrarEventos(@RequestBody Map<String, Object> filtros) {
        // Obtener los parámetros de los filtros
        String lugar = (String) filtros.get("lugar");
        String fechaStr = (String) filtros.get("fecha");
        String categoria = (String) filtros.get("categoria");
        String artista = (String) filtros.get("artista");

        // Convertir la fecha de String a LocalDate
        LocalDate fecha = null;
        if (fechaStr != null && !fechaStr.isEmpty()) {
            try {
                fecha = LocalDate.parse(fechaStr);
            } catch (DateTimeParseException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha inválida", e);
            }
        }
        
        // Llamar al servicio de filtrado
        return eventoService.filtrarEventos(lugar, fecha, categoria, artista);
    }
    @PostMapping("/pagar")
    public ResponseEntity<String> procesarPago(@RequestBody PagoRequest pagoRequest) {
        // Lógica de procesamiento del pago
        return ResponseEntity.ok("Pago procesado con éxito");
    }
    
    //NUEVOS ENDPOINT
    
    @GetMapping("/mis-eventos")
    public ResponseEntity<List<Evento>> getMisEventos(Authentication authentication) {
        // Verificar autenticación
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Obtener usuario
        Usuario usuarioActual = (Usuario) authentication.getPrincipal();
        System.out.println("Usuario autenticado ID: " + usuarioActual.getIdUsuario());
        
        // Obtener eventos
        List<Evento> eventos = eventoService.obtenerEventosPorCreador(usuarioActual.getIdUsuario());
        System.out.println("Eventos encontrados: " + eventos.size());
        
        return ResponseEntity.ok(eventos);
    }
    @GetMapping("/mis-eventos/paginados")
    public ResponseEntity<Page<Evento>> getMisEventosPaginados(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Usuario usuarioActual = (Usuario) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        Page<Evento> eventos = eventoService.obtenerEventosPorCreadorId(usuarioActual.getIdUsuario(), pageable);
        
        return ResponseEntity.ok(eventos);
    }
    }

