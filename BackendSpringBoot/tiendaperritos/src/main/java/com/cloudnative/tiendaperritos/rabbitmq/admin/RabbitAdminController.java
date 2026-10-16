package com.cloudnative.tiendaperritos.rabbitmq.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rabbit")
@CrossOrigin(origins = "http://localhost:5173")
public class RabbitAdminController {

    private final RabbitAdminService service;

    public RabbitAdminController(RabbitAdminService service) {
        this.service = service;
    }

    @PostMapping("/queues")
    public ResponseEntity<String> crearCola(@Valid @RequestBody QueueRequest req) {
        service.crearCola(req);
        return ResponseEntity.status(HttpStatus.CREATED).body("Cola creada: " + req.name());
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<String> eliminarCola(@PathVariable String name) {
        return service.eliminarCola(name)
                ? ResponseEntity.ok("Cola eliminada: " + name)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("La cola no existe: " + name);
    }

    @PostMapping("/exchanges")
    public ResponseEntity<String> crearExchange(@Valid @RequestBody ExchangeRequest req) {
        service.crearExchange(req);
        return ResponseEntity.status(HttpStatus.CREATED).body("Exchange creado: " + req.name());
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<String> eliminarExchange(@PathVariable String name) {
        return service.eliminarExchange(name)
                ? ResponseEntity.ok("Exchange eliminado: " + name)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("El exchange no existe: " + name);
    }

    @PostMapping("/bindings")
    public ResponseEntity<String> crearBinding(@Valid @RequestBody BindingRequest req) {
        service.crearBinding(req);
        return ResponseEntity.status(HttpStatus.CREATED).body("Binding creado");
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<String> eliminarBinding(@Valid @RequestBody BindingRequest req) {
        service.eliminarBinding(req);
        return ResponseEntity.ok("Binding eliminado");
    }
}