package com.cloudnative.tiendaperritos.rabbitmq.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.stereotype.Service;

@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);

    private final AmqpAdmin amqpAdmin;

    public RabbitAdminService(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    public void crearCola(QueueRequest req) {
        QueueBuilder builder = req.durable()
                ? QueueBuilder.durable(req.name())
                : QueueBuilder.nonDurable(req.name());
        if (req.deadLetterExchange() != null && !req.deadLetterExchange().isBlank()) {
            builder.withArgument("x-dead-letter-exchange", req.deadLetterExchange());
            if (req.deadLetterRoutingKey() != null && !req.deadLetterRoutingKey().isBlank()) {
                builder.withArgument("x-dead-letter-routing-key", req.deadLetterRoutingKey());
            }
        }
        amqpAdmin.declareQueue(builder.build());
        log.info("Cola creada: {}", req.name());
    }

    public boolean eliminarCola(String name) {
        boolean eliminada = amqpAdmin.deleteQueue(name);
        log.info("Eliminar cola {}: {}", name, eliminada);
        return eliminada;
    }

    public void crearExchange(ExchangeRequest req) {
        Exchange exchange = switch (req.type()) {
            case "topic" -> new TopicExchange(req.name(), req.durable(), false);
            case "fanout" -> new FanoutExchange(req.name(), req.durable(), false);
            default -> new DirectExchange(req.name(), req.durable(), false);
        };
        amqpAdmin.declareExchange(exchange);
        log.info("Exchange creado: {} ({})", req.name(), req.type());
    }

    public boolean eliminarExchange(String name) {
        boolean eliminado = amqpAdmin.deleteExchange(name);
        log.info("Eliminar exchange {}: {}", name, eliminado);
        return eliminado;
    }

    public void crearBinding(BindingRequest req) {
        String key = req.routingKey() == null ? "" : req.routingKey();
        Binding binding = new Binding(req.queue(), Binding.DestinationType.QUEUE,
                req.exchange(), key, null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding creado: {} -> {} ({})", req.exchange(), req.queue(), key);
    }

    public void eliminarBinding(BindingRequest req) {
        String key = req.routingKey() == null ? "" : req.routingKey();
        Binding binding = new Binding(req.queue(), Binding.DestinationType.QUEUE,
                req.exchange(), key, null);
        amqpAdmin.removeBinding(binding);
        log.info("Binding eliminado: {} -> {} ({})", req.exchange(), req.queue(), key);
    }
}