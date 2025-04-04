package com.tu_paquete.ticketflex.Controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.tu_paquete.ticketflex.Model.Evento;
import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.Repository.UsuarioRepository;
import com.tu_paquete.ticketflex.Service.EventoService;

import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
	private EventoService eventoService;
	
	@Autowired
    private UsuarioRepository usuarioRepository;

	@GetMapping("/dashboard")
	public String dashboard(Model model, Authentication authentication) {
	    // Obtener el usuario autenticado
	    String email = authentication.getName();
	    Usuario usuario = usuarioRepository.findByEmail(email)
	                          .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
	    
	    // Obtener solo los eventos del usuario autenticado
	    List<Evento> eventos = eventoService.obtenerEventosPorCreador(usuario.getIdUsuario());
	    
	    model.addAttribute("eventos", eventos);
	    return "admin/dashboard";
	}

	@GetMapping("/eventos/crear")
	public String mostrarFormularioCrearEvento(Model model) {
	    model.addAttribute("evento", new Evento());  // Agrega un objeto Evento vacío al modelo
	    
	    // Lista de categorías predefinidas
	    List<String> categorias = Arrays.asList("Concierto", "Festival", "Teatro", "Conferencia", "Feria");
	    model.addAttribute("categorias", categorias);  // Enviar la lista a la vista
	    
	    return "admin/crear-evento";  // Renderiza la vista del formulario
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
        return "redirect:/admin/eventos/listar";
    }

    @GetMapping("/eventos/listar")
    public String listarEventos(Model model) {
        // Aquí puedes obtener la lista de eventos desde el servicio y pasarla al modelo
        return "admin/listar-eventos"; // Renderiza la lista de eventos
    }

 // Método GET para mostrar el formulario de edición
    @GetMapping("/eventos/editar/{id}")
    public String mostrarFormularioEditarEvento(@PathVariable Integer id, Model model) {
        Evento evento = eventoService.obtenerEventorPorId(id);  // Obtener el evento por ID
        if (evento == null) {
            return "redirect:/admin/eventos/listar";  // Redirigir si el evento no existe
        }
        model.addAttribute("evento", evento);  // Agregar el evento al modelo
        return "admin/editar-evento";  // Renderizar la vista del formulario de edición
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
    public String mostrarEstadisticas(Model model) {
        // Aquí puedes obtener estadísticas desde el servicio y pasarlas al modelo
        return "admin/estadisticas"; // Renderiza la vista de estadísticas
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
}