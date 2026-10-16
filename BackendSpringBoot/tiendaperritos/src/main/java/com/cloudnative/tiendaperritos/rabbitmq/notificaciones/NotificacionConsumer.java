package com.cloudnative.tiendaperritos.rabbitmq.notificaciones;

import com.cloudnative.tiendaperritos.rabbitmq.config.MensajeriaProperties;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificacionConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificacionConsumer.class);
    private static final String HEADER_REINTENTOS = "x-reintentos";

    private final RabbitTemplate rabbitTemplate;
    private final MensajeriaProperties.Canal canal;
    private final int maxReintentos;

    public NotificacionConsumer(RabbitTemplate rabbitTemplate,
                                MensajeriaProperties props,
                                @Value("${app.mensajeria.max-reintentos}") int maxReintentos) {
        this.rabbitTemplate = rabbitTemplate;
        this.canal = props.notificaciones();
        this.maxReintentos = maxReintentos;
    }

    @RabbitListener(queues = "${app.mensajeria.notificaciones.queue}")
    public void recibir(NotificacionMensaje mensaje, Message raw, Channel channel) throws IOException {
        long tag = raw.getMessageProperties().getDeliveryTag();
        try {
            procesar(mensaje);
            channel.basicAck(tag, false);
        } catch (IllegalArgumentException e) {
            // No recuperable: directo a DLQ
            log.error("Mensaje invalido, enviado a DLQ: {}", e.getMessage());
            channel.basicNack(tag, false, false);
        } catch (ErrorRecuperableException e) {
            Integer intentos = raw.getMessageProperties().getHeader(HEADER_REINTENTOS);
            int actual = intentos == null ? 0 : intentos;
            if (actual < maxReintentos) {
                log.warn("Fallo recuperable ({}). Reintento {}/{}", e.getMessage(), actual + 1, maxReintentos);
                channel.basicAck(tag, false);
                rabbitTemplate.convertAndSend(canal.exchange(), canal.routingKey(), mensaje, m -> {
                    m.getMessageProperties().setHeader(HEADER_REINTENTOS, actual + 1);
                    return m;
                });
            } else {
                log.error("Reintentos agotados ({}), enviado a DLQ: {}", maxReintentos, e.getMessage());
                channel.basicNack(tag, false, false);
            }
        } catch (Exception e) {
            log.error("Error inesperado, enviado a DLQ: {}", e.getMessage());
            channel.basicNack(tag, false, false);
        }
    }

    private void procesar(NotificacionMensaje mensaje) {
        if (mensaje.destinatario() == null || mensaje.destinatario().isBlank()) {
            throw new IllegalArgumentException("Destinatario vacio");
        }
        // Simulacion de fallo temporal para la demo
        if ("falla@temporal.cl".equals(mensaje.destinatario())) {
            throw new ErrorRecuperableException("Servicio de correo no disponible");
        }
        log.info("Notificacion enviada a {}: {}", mensaje.destinatario(), mensaje.asunto());
    }
}