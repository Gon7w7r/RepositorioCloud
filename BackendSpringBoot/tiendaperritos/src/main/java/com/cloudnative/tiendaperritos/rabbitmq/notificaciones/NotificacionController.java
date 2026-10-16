package com.cloudnative.tiendaperritos.rabbitmq.notificaciones;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificacionController {

    private final NotificacionProductor productor;

    public NotificacionController(NotificacionProductor productor) {
        this.productor = productor;
    }

    @PostMapping
    public ResponseEntity<String> enviar(@RequestBody NotificacionMensaje mensaje) {
        productor.enviar(mensaje);
        return ResponseEntity.accepted().body("Notificacion encolada");
    }
}