package com.cloudnative.tiendaperritos.rabbitmq.tickets;

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
public class TicketConsumer {

    private static final Logger log = LoggerFactory.getLogger(TicketConsumer.class);
    private static final String HEADER_REINTENTOS = "x-reintentos";

    private final RabbitTemplate rabbitTemplate;
    private final MensajeriaProperties.Canal canal;
    private final int maxReintentos;

    public TicketConsumer(RabbitTemplate rabbitTemplate,
                          MensajeriaProperties props,
                          @Value("${app.mensajeria.max-reintentos}") int maxReintentos) {
        this.rabbitTemplate = rabbitTemplate;
        this.canal = props.tickets();
        this.maxReintentos = maxReintentos;
    }

    @RabbitListener(queues = "${app.mensajeria.tickets.queue}")
    public void recibir(TicketMensaje mensaje, Message raw, Channel channel) throws IOException {
        long tag = raw.getMessageProperties().getDeliveryTag();
        try {
            procesar(mensaje);
            channel.basicAck(tag, false);
        } catch (IllegalArgumentException e) {
            log.error("Ticket invalido, enviado a DLQ: {}", e.getMessage());
            channel.basicNack(tag, false, false);
        } catch (IllegalStateException e) {
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
                log.error("Reintentos agotados ({}), ticket enviado a DLQ: {}", maxReintentos, e.getMessage());
                channel.basicNack(tag, false, false);
            }
        } catch (Exception e) {
            log.error("Error inesperado en ticket, enviado a DLQ: {}", e.getMessage());
            channel.basicNack(tag, false, false);
        }
    }

    private void procesar(TicketMensaje mensaje) {
        if (mensaje.pedidoId() == null || mensaje.total() == null || mensaje.total() <= 0) {
            throw new IllegalArgumentException("Pedido o total invalido");
        }
        // Simulacion de fallo temporal para la demo
        if (mensaje.pedidoId() == 999L) {
            throw new IllegalStateException("Generador de documentos no disponible");
        }
        log.info("Ticket generado para pedido {} (cliente {}, total {})",
                mensaje.pedidoId(), mensaje.cliente(), mensaje.total());
    }
}