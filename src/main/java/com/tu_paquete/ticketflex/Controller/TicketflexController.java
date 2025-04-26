/*package com.tu_paquete.ticketflex.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.tu_paquete.ticketflex.Model.TicketflexData;
import com.tu_paquete.ticketflex.Service.TicketflexService;
import com.tu_paquete.ticketflex.Service.dto.PredictionTicketflex;


@Controller
@RequestMapping("/prediccion")
public class TicketflexController {

    private final TicketflexService ticketflexService;

    public TicketflexController(TicketflexService ticketflexService) {
        this.ticketflexService = ticketflexService;
    }

    @GetMapping
    public String showForm(Model model) {
        model.addAttribute("ticketflexData", new TicketflexData());
        model.addAttribute("generos", new String[] {"Masculino", "Femenino", "Otro"});
        model.addAttribute("intereses", new String[] {"Alto", "Bajo", "Medio"});
        model.addAttribute("canales", new String[] {"App", "Online", "Taquilla"});
        return "Ticketflex";
    }

    @PostMapping("/predecir")
    public String predecir(@ModelAttribute TicketflexData datos, Model model) {
        try {
            PredictionTicketflex prediction = ticketflexService.predictAndSave(datos);

            model.addAttribute("prediction", prediction);
            model.addAttribute("prediccion", prediction.getPrediction());
            model.addAttribute("confianza", prediction.getConfidence());
            model.addAttribute("confianzaValue", prediction.getConfidenceValue());
            model.addAttribute("ticketflexData", datos);
            model.addAttribute("showResult", true);

            return "Ticketflex";
        } catch (Exception e) {
            ticketflexService.logError("Error al realizar la predicción", e);
            model.addAttribute("error", "Error interno al procesar la predicción: " + e.getMessage());
            model.addAttribute("ticketflexData", datos);
            return "Ticketflex";
        }
    }
}*/