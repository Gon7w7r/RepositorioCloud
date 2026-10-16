package com.cloudnative.tiendaperritos.rabbitmq.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record QueueRequest(
        @NotBlank(message = "El nombre de la cola es obligatorio")
        @Pattern(regexp = "^[a-zA-Z0-9._-]{1,100}$",
                 message = "Nombre invalido: use letras, numeros, punto, guion o guion bajo (max 100)")
        String name,
        boolean durable,
        String deadLetterExchange,
        String deadLetterRoutingKey) {}