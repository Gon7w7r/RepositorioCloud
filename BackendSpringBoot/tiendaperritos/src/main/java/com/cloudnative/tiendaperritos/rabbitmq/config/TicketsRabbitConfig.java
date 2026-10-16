package com.cloudnative.tiendaperritos.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TicketsRabbitConfig {

    private final MensajeriaProperties.Canal canal;

    public TicketsRabbitConfig(MensajeriaProperties props) {
        this.canal = props.tickets();
    }

    @Bean
    DirectExchange ticketsExchange() {
        return new DirectExchange(canal.exchange(), true, false);
    }

    @Bean
    DirectExchange ticketsDlx() {
        return new DirectExchange(canal.dlx(), true, false);
    }

    @Bean
    Queue ticketsQueue() {
        return QueueBuilder.durable(canal.queue())
                .withArgument("x-dead-letter-exchange", canal.dlx())
                .withArgument("x-dead-letter-routing-key", canal.dlqRoutingKey())
                .build();
    }

    @Bean
    Queue ticketsDlq() {
        return QueueBuilder.durable(canal.dlq()).build();
    }

    @Bean
    Binding ticketsBinding() {
        return BindingBuilder.bind(ticketsQueue())
                .to(ticketsExchange())
                .with(canal.routingKey());
    }

    @Bean
    Binding ticketsDlqBinding() {
        return BindingBuilder.bind(ticketsDlq())
                .to(ticketsDlx())
                .with(canal.dlqRoutingKey());
    }
}