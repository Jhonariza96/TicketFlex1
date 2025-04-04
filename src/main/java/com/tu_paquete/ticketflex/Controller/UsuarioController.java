package com.tu_paquete.ticketflex.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tu_paquete.ticketflex.Model.Transaccion;
import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.Service.TransaccionService;
import com.tu_paquete.ticketflex.Service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private TransaccionService transaccionService;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario usuario) {
        Map<String, String> response = new HashMap<>();
        try {
            // Validación básica
            if(usuario.getNombre() == null || usuario.getNombre().trim().isEmpty() ||
               usuario.getEmail() == null || usuario.getEmail().trim().isEmpty() ||
               usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
                
                response.put("error", "Nombre, email y contraseña son obligatorios");
                return ResponseEntity.badRequest().body(response);
            }

            Usuario usuarioRegistrado = usuarioService.registrarUsuario(usuario);
            response.put("mensaje", "Usuario registrado exitosamente");
            response.put("idUsuario", usuarioRegistrado.getIdUsuario().toString());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("error", "Error interno: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }




    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> iniciarSesion(@RequestParam String email, @RequestParam String password) {
        Usuario usuario = usuarioService.iniciarSesion(email, password);
        if (usuario != null) {
        	Map<String, Object> response = new HashMap<>();
        	response.put("id", usuario.getIdUsuario());
        	response.put("nombre", usuario.getNombre());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(null);
    }
    
 // Manejar solicitudes GET con un mensaje adecuado
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ResponseEntity<String> loginGet() {
        // Puedes devolver un mensaje de error o una página de error
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                             .body("Método GET no permitido.");
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> cerrarSesion() {
        // Aquí puedes invalidar la sesión del usuario
        return ResponseEntity.ok("Sesión cerrada exitosamente");
    }
    
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<Transaccion>> obtenerHistorialDeCompras(@PathVariable Integer id) {
        try {
            // Obtener las transacciones del usuario
            List<Transaccion> historial = transaccionService.obtenerHistorialDeCompras(id);

            // Si el historial está vacío, respondemos con un 204 No Content
            if (historial.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            }

            // De lo contrario, respondemos con el historial de compras
            return ResponseEntity.ok(historial);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}

