package com.tu_paquete.ticketflex.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tu_paquete.ticketflex.Model.Transaccion;
import com.tu_paquete.ticketflex.Repository.TransaccionRepository;
import com.tu_paquete.ticketflex.Service.TransaccionService;

import jakarta.transaction.Transactional;

import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {
    @Autowired
    private TransaccionService transaccionService;
    
    @Autowired
    private TransaccionRepository transaccionRepository;
    
    @Transactional
    @PostMapping("/crear")
    public ResponseEntity<Transaccion> crearTransaccion(@RequestBody Transaccion transaccion) {
        try {
            if (transaccion.getFechaPago() == null) {
                transaccion.setFechaPago(new Timestamp(System.currentTimeMillis()));
            }
            if (transaccion.getEstadoPago() == null) {
                transaccion.setEstadoPago("pendiente");
            }

            System.out.println("Transacción a guardar: " + transaccion);
            Transaccion nuevaTransaccion = transaccionRepository.save(transaccion);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaTransaccion);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/listar")
    public List<Transaccion> listarTransacciones() {
        return transaccionService.listarTransacciones();
    }

    @GetMapping("/{id}")
    public Transaccion obtenerTransaccionPorId(@PathVariable Integer id) {
        return transaccionService.obtenerTransaccionPorId(id);
    }

    @DeleteMapping("/{id}")
    public void eliminarTransaccion(@PathVariable Integer id) {
        transaccionService.eliminarTransaccion(id);
    }
    
    @GetMapping("/historial/{idUsuario}")
    public ResponseEntity<List<Transaccion>> obtenerHistorialDeCompras(@PathVariable Integer idUsuario) {
        List<Transaccion> historial = transaccionService.obtenerHistorialDeCompras(idUsuario);
        if (historial.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
        return ResponseEntity.ok(historial);
    }

}





