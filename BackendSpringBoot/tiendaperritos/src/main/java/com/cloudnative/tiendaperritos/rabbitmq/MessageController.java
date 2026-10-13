package com.cloudnative.tiendaperritos.rabbitmq;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final Sender sender;

    public MessageController(Sender sender) {
        this.sender = sender;
    }

    @PostMapping
    public ResponseEntity<String> send(@RequestBody MessageRequest request) {
        sender.sendMessage(request.message());
        return ResponseEntity.ok("Mensaje enviado: " + request.message());
    }

    public record MessageRequest(String message) {}
}