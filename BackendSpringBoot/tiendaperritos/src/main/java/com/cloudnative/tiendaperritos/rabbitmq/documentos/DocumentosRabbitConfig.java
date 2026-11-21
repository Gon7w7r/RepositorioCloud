package com.cloudnative.tiendaperritos.rabbitmq.documentos;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudnative.tiendaperritos.rabbitmq.config.MensajeriaProperties;

@Configuration 
public class DocumentosRabbitConfig {

    private final MensajeriaProperties.Canal canal;

    public DocumentosRabbitConfig(MensajeriaProperties props) {
        this.canal = props.documentos();
    }

    @Bean 
    DirectExchange documentosExchange() {
        return new DirectExchange(canal.exchange(), true, false);
    }

    @Bean
    DirectExchange documentosDlx() {
        return new DirectExchange(canal.dlx(), true, false);
    }

    @Bean
    Queue documentosQueue() {
        return QueueBuilder.durable(canal.queue())
                .withArgument("x-dead-letter-exchange", canal.dlx())
                .withArgument("x-dead-letter-routing-key", canal.dlqRoutingKey())
                .build();
    }

    @Bean
    Queue documentosDlq() {
        return QueueBuilder.durable(canal.dlq()).build();
    }

    @Bean
    Binding documentosBinding() {
        return BindingBuilder.bind(documentosQueue())
                .to(documentosExchange())
                .with(canal.routingKey());
    }

    @Bean
    Binding documentosDlqBinding() {
        return BindingBuilder.bind(documentosDlq())
                .to(documentosDlx())
                .with(canal.dlqRoutingKey());
    }
}