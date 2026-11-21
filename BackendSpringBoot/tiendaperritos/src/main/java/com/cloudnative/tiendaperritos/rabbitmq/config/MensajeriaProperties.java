package com.cloudnative.tiendaperritos.rabbitmq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mensajeria")
public record MensajeriaProperties(Canal notificaciones, Canal tickets, Canal documentos) {

    public record Canal(
            String exchange,
            String queue,
            String routingKey,
            String bindingKey,
            String dlx,
            String dlq,
            String dlqRoutingKey) {}
}