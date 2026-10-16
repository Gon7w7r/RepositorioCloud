package com.cloudnative.tiendaperritos.rabbitmq.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ExchangeRequest(
        @NotBlank(message = "El nombre del exchange es obligatorio")
        @Pattern(regexp = "^[a-zA-Z0-9._-]{1,100}$",
                 message = "Nombre invalido: use letras, numeros, punto, guion o guion bajo (max 100)")
        String name,
        @NotBlank(message = "El tipo es obligatorio")
        @Pattern(regexp = "^(direct|topic|fanout)$",
                 message = "Tipo invalido: use direct, topic o fanout")
        String type,
        boolean durable) {}