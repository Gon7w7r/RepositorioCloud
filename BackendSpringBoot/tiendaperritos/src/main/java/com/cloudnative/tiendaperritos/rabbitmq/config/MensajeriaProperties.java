package com.cloudnative.tiendaperritos.rabbitmq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mensajeria")
public record MensajeriaProperties(Canal notificaciones, Canal tickets) {

    public record Canal(
            String exchange,
            String queue,
            String routingKey,
            String dlx,
            String dlq,
            String dlqRoutingKey) {}
}