package com.cloudnative.tiendaperritos.rabbitmq.notificaciones;

import com.cloudnative.tiendaperritos.rabbitmq.config.MensajeriaProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificacionProductor {

    private final RabbitTemplate rabbitTemplate;
    private final MensajeriaProperties.Canal canal;

    public NotificacionProductor(RabbitTemplate rabbitTemplate, MensajeriaProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.canal = props.notificaciones();
    }

    public void enviar(NotificacionMensaje mensaje) {
        rabbitTemplate.convertAndSend(canal.exchange(), canal.routingKey(), mensaje);
    }
}