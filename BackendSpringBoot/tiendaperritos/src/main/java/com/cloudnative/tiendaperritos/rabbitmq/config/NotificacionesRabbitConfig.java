package com.cloudnative.tiendaperritos.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificacionesRabbitConfig {

    private final MensajeriaProperties.Canal canal;

    public NotificacionesRabbitConfig(MensajeriaProperties props) {
        this.canal = props.notificaciones();
    }

    @Bean
    DirectExchange notificacionesExchange() {
        return new DirectExchange(canal.exchange(), true, false);
    }

    @Bean
    DirectExchange notificacionesDlx() {
        return new DirectExchange(canal.dlx(), true, false);
    }

    @Bean
    Queue notificacionesQueue() {
        return QueueBuilder.durable(canal.queue())
                .withArgument("x-dead-letter-exchange", canal.dlx())
                .withArgument("x-dead-letter-routing-key", canal.dlqRoutingKey())
                .build();
    }

    @Bean
    Queue notificacionesDlq() {
        return QueueBuilder.durable(canal.dlq()).build();
    }

    @Bean
    Binding notificacionesBinding() {
        return BindingBuilder.bind(notificacionesQueue())
                .to(notificacionesExchange())
                .with(canal.routingKey());
    }

    @Bean
    Binding notificacionesDlqBinding() {
        return BindingBuilder.bind(notificacionesDlq())
                .to(notificacionesDlx())
                .with(canal.dlqRoutingKey());
    }
}