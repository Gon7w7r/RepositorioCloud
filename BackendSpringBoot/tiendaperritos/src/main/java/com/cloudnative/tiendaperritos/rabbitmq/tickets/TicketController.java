package com.cloudnative.tiendaperritos.rabbitmq.tickets;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "http://localhost:5173")
public class TicketController {

    private final TicketProductor productor;

    public TicketController(TicketProductor productor) {
        this.productor = productor;
    }

    @PostMapping
    public ResponseEntity<String> generar(@RequestBody TicketMensaje mensaje) {
        productor.enviar(mensaje);
        return ResponseEntity.accepted().body("Ticket encolado");
    }
}