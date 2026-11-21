package com.cloudnative.tiendaperritos.rabbitmq.documentos;


import com.cloudnative.tiendaperritos.rabbitmq.config.MensajeriaProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class DocumentoProductor {

    private final RabbitTemplate rabbitTemplate;
    private final MensajeriaProperties.Canal canal;

    public DocumentoProductor(RabbitTemplate rabbitTemplate, MensajeriaProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.canal = props.documentos();
    }

    public void enviar(DocumentoMensaje mensaje) {
        rabbitTemplate.convertAndSend(canal.exchange(), canal.routingKey(), mensaje);
    }
}