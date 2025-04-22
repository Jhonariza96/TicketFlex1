package com.tu_paquete.ticketflex.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tu_paquete.ticketflex.PagoRequest;
import com.tu_paquete.ticketflex.Model.Boleto;
import com.tu_paquete.ticketflex.Service.BoletoService;

@RestController
@RequestMapping("/api/boletas")
public class BoletoController {
    @Autowired
    private BoletoService boletoService;

    @PostMapping("/comprar")
    public ResponseEntity<?> comprarBoleto(@RequestParam Integer idEvento, 
                                         @RequestParam Integer idUsuario, 
                                         @RequestParam Integer cantidad) {
        try {
            Boleto boleto = boletoService.comprarBoleto(idEvento, idUsuario, cantidad);
            return ResponseEntity.ok(boleto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // Nuevo endpoint para manejar el pago con tarjeta
    @PostMapping("/pagar-tarjeta")
    public String procesarPago(@RequestBody PagoRequest pagoRequest) {
        // Aquí procesas el pago con el banco seleccionado, tarjeta, etc.
        boolean pagoExitoso = boletoService.procesarPago(pagoRequest);

        if (pagoExitoso) {
            return "Pago procesado exitosamente";
        } else {
            return "Error en el procesamiento del pago";
        }
    }
}

