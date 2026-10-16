package com.cloudnative.tiendaperritos.rabbitmq.tickets;

import com.cloudnative.tiendaperritos.rabbitmq.config.MensajeriaProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class TicketProductor {

    private final RabbitTemplate rabbitTemplate;
    private final MensajeriaProperties.Canal canal;

    public TicketProductor(RabbitTemplate rabbitTemplate, MensajeriaProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.canal = props.tickets();
    }

    public void enviar(TicketMensaje mensaje) {
        rabbitTemplate.convertAndSend(canal.exchange(), canal.routingKey(), mensaje);
    }
}