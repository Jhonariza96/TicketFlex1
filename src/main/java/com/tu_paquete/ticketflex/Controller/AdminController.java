package com.tu_paquete.ticketflex.Controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.tu_paquete.ticketflex.Model.Boleto;
import com.tu_paquete.ticketflex.Model.Evento;
import com.tu_paquete.ticketflex.Model.TicketflexPrediccion;
import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.Repository.BoletoRepository;
import com.tu_paquete.ticketflex.Repository.EventoRepository;
import com.tu_paquete.ticketflex.Repository.UsuarioRepository;
import com.tu_paquete.ticketflex.Service.EventoService;
import com.tu_paquete.ticketflex.Service.TicketflexService;
import com.tu_paquete.ticketflex.Service.dto.EventoRequest;
import com.tu_paquete.ticketflex.Service.dto.PrediccionEventoResponse;
import com.tu_paquete.ticketflex.Service.dto.PredictionTicketflex;
import com.tu_paquete.ticketflex.dto.EventoConEstadisticas;

import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private EventoService eventoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BoletoRepository boletoRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private TicketflexService ticketflexService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        // Obtener el usuario autenticado
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Obtener solo los eventos del usuario autenticado
        List<Evento> eventos = eventoService.obtenerEventosPorCreador(usuario.getIdUsuario());
        Long boletosVendidos = eventoService.contarBoletosVendidosPorCreador(usuario.getIdUsuario()); // Nuevo

        // Agregar atributos al modelo
        model.addAttribute("nombreUsuario", usuario.getNombre()); // Asumiendo que tienes getNombre()
        model.addAttribute("apellidoUsuario", usuario.getApellido()); // Si necesitas el apellido
        model.addAttribute("emailUsuario", usuario.getEmail()); // Si necesitas el email
        model.addAttribute("eventos", eventos);
        model.addAttribute("totalBoletosVendidos", boletosVendidos); // Envía el total filtrado

        return "admin/dashboard";
    }

    @GetMapping("/eventos/crear")
    public String mostrarFormularioCrearEvento(Model model) {
        model.addAttribute("evento", new Evento()); // Agrega un objeto Evento vacío al modelo

        // Lista de categorías predefinidas
        List<String> categorias = Arrays.asList("Concierto", "Festival", "Teatro", "Conferencia", "Feria");
        model.addAttribute("categorias", categorias); // Enviar la lista a la vista

        return "admin/crear-evento"; // Renderiza la vista del formulario
    }

    // Método POST para procesar el formulario de creación
    @PostMapping("/eventos/crear")
    public String crearEvento(@ModelAttribute Evento evento,
            Model model,
            Authentication authentication) {

        // Obtener el usuario autenticado
        String email = authentication.getName();
        Usuario creador = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Asignar el creador al evento
        evento.setCreador(creador);

        eventoService.crearEvento(evento, email);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/eventos/listar")
    public String listarEventos(Model model) {
        // Aquí puedes obtener la lista de eventos desde el servicio y pasarla al modelo
        return "admin/listar-eventos"; // Renderiza la lista de eventos
    }

    // Método GET para mostrar el formulario de edición
    @GetMapping("/eventos/editar/{id}")
    public String mostrarFormularioEditarEvento(@PathVariable Integer id, Model model) {
        Evento evento = eventoService.obtenerEventorPorId(id); // Obtener el evento por ID
        if (evento == null) {
            return "redirect:/admin/eventos/listar"; // Redirigir si el evento no existe
        }
        model.addAttribute("evento", evento); // Agregar el evento al modelo
        return "admin/editar-evento"; // Renderizar la vista del formulario de edición
    }

    // Método POST para procesar el formulario de edición
    @PostMapping("/eventos/editar/{id}")
    public String actualizarEvento(
            @PathVariable Integer id,
            @ModelAttribute Evento eventoActualizado,
            Authentication authentication) {

        // 1. Obtener el evento existente con su creador
        Evento eventoExistente = eventoService.obtenerEventorPorId(id);

        // 2. Verificar que existe
        if (eventoExistente == null) {
            return "redirect:/admin/dashboard?error=evento_no_existe";
        }

        // 3. Mantener el creador original
        eventoActualizado.setCreador(eventoExistente.getCreador());

        // 4. Actualizar campos editables
        eventoExistente.setNombreEvento(eventoActualizado.getNombreEvento());
        eventoExistente.setFecha(eventoActualizado.getFecha());
        eventoExistente.setLugar(eventoActualizado.getLugar());
        eventoExistente.setDescripcion(eventoActualizado.getDescripcion());
        eventoExistente.setPrecioBase(eventoActualizado.getPrecioBase());
        eventoExistente.setDisponibilidad(eventoActualizado.getDisponibilidad());
        eventoExistente.setCategoria(eventoActualizado.getCategoria());
        eventoExistente.setArtista(eventoActualizado.getArtista());

        // 5. Manejo especial de imagen
        if (eventoActualizado.getImagen() != null && !eventoActualizado.getImagen().isEmpty()) {
            eventoExistente.setImagen(eventoActualizado.getImagen());
        }

        // 6. Guardar cambios
        eventoService.actualizarEvento(eventoExistente);

        return "redirect:/admin/dashboard?success=evento_actualizado";
    }

    @GetMapping("/estadisticas")
    public String mostrarEstadisticas(Model model, Authentication authentication) {
        // Obtener el usuario/admin logueado
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 1. Obtener solo los eventos del administrador actual
        List<EventoConEstadisticas> eventos = eventoRepository.findEventosConEstadisticas(usuario.getIdUsuario());

        // 2. Calcular estadísticas SOLO para los eventos de este administrador
        Long totalBoletosVendidos = eventos.stream()
                .mapToLong(EventoConEstadisticas::getBoletosVendidos)
                .sum();

        BigDecimal ingresosTotales = eventos.stream()
                .map(EventoConEstadisticas::getIngresos)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double ocupacionPromedio = eventos.stream()
                .mapToInt(EventoConEstadisticas::getPorcentajeOcupacion)
                .average()
                .orElse(0.0);

        model.addAttribute("totalBoletosVendidos", totalBoletosVendidos);
        model.addAttribute("ingresosTotales", ingresosTotales);
        model.addAttribute("ocupacionPromedio", Math.round(ocupacionPromedio));
        model.addAttribute("eventos", eventos);

        return "admin/estadisticas";
    }

    
    

    // Método GET para eliminar el evento
    @GetMapping("/eventos/eliminar/{id}")
    public String eliminarEvento(@PathVariable Integer id) {
        // Obtener el evento por ID
        Evento evento = eventoService.obtenerEventorPorId(id);

        if (evento != null) {
            // Eliminar el evento
            eventoService.eliminarEvento(id);
        }

        // Redirigir a la lista de eventos después de eliminar
        return "redirect:/admin/eventos/listar";
    }

    /* Predicción de Ticketflex (Formulario de predicción) */
    @GetMapping("/prediccion")
    public String showForm(Model model) {
        model.addAttribute("ticketflexData", new TicketflexPrediccion());
        model.addAttribute("generos", new String[] { "Femenino", "Masculino", "Otro" });
        model.addAttribute("generosMusicales", new String[] {
                "Reggaeton", "Pop", "Rock", "Salsa", "KPop", "Metal", "Electronica"
        });
        return "admin/prediccion"; // Página para realizar la predicción
    }

    @PostMapping("/prediccion/predecir")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> predecir(@RequestBody TicketflexPrediccion datos) {
        try {
            PredictionTicketflex prediction = ticketflexService.predictAndSave(datos);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("prediccion", prediction.getPrediction());
            response.put("confianza", prediction.getConfidence());
            response.put("confianzaValue", prediction.getConfidenceValue());
            response.put("datosCliente", datos);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al procesar la predicción");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.internalServerError()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    @PostMapping("/predecir-masivo")
    public ResponseEntity<?> predecirAsistenciaMasiva(
            @RequestBody EventoRequest eventoRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false, defaultValue = "false") boolean mostrarTodos) {

        try {
            // Validación básica
            if (eventoRequest.getGenero() == null || eventoRequest.getPrecio() == null) {
                throw new IllegalArgumentException("Datos requeridos no proporcionados");
            }

            Page<PrediccionEventoResponse> resultados = ticketflexService
                    .predecirAsistenciaMasivaPaginada(eventoRequest, page, size, mostrarTodos);

            // Respuesta paginada mejorada
            Map<String, Object> response = new HashMap<>();
            response.put("content", resultados.getContent());
            response.put("totalElements", resultados.getTotalElements());
            response.put("totalPages", resultados.getTotalPages());
            response.put("currentPage", resultados.getNumber());
            response.put("mostrarTodos", mostrarTodos); // Incluir el estado del filtro

            return ResponseEntity.ok()
                    .header("X-Total-Elements", String.valueOf(resultados.getTotalElements()))
                    .body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Datos inválidos",
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Error en predicción masiva",
                    "message", "Ocurrió un error al procesar la solicitud",
                    "timestamp", LocalDateTime.now()));
        }
    }

    
}
