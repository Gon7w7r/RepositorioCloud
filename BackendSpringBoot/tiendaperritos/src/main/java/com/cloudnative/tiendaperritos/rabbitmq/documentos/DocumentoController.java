package com.cloudnative.tiendaperritos.rabbitmq.documentos;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;




@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/documentos")
public class DocumentoController {

    private final DocumentoProductor productor;

    public DocumentoController(DocumentoProductor productor) {
        this.productor = productor;
    }

    @PostMapping
    public ResponseEntity<String> generar(@RequestBody DocumentoMensaje mensaje) {
        productor.enviar(mensaje);
        return ResponseEntity.accepted().body("Documento encolado");
    }
}