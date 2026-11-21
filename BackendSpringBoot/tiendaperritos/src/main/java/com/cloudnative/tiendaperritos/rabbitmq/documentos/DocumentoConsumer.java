package com.cloudnative.tiendaperritos.rabbitmq.documentos;



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
public class DocumentoConsumer {

    private static final Logger log = LoggerFactory.getLogger(DocumentoConsumer.class);
    private static final String HEADER_REINTENTOS = "x-reintentos";

    private final RabbitTemplate rabbitTemplate;
    private final MensajeriaProperties.Canal canal;
    private final int maxReintentos;

    public DocumentoConsumer(RabbitTemplate rabbitTemplate,
                             MensajeriaProperties props,
                             @Value("${app.mensajeria.max-reintentos}") int maxReintentos) {
        this.rabbitTemplate = rabbitTemplate;
        this.canal = props.documentos();
        this.maxReintentos = maxReintentos;
    }

    @RabbitListener(queues = "${app.mensajeria.documentos.queue}")
    public void recibir(DocumentoMensaje mensaje, Message raw, Channel channel) throws IOException {
        long tag = raw.getMessageProperties().getDeliveryTag();
        try {
            procesar(mensaje);
            channel.basicAck(tag, false);
        } catch (IllegalArgumentException e) {
            log.error("Documento invalido, enviado a DLQ: {}", e.getMessage());
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
                log.error("Reintentos agotados ({}), documento enviado a DLQ: {}", maxReintentos, e.getMessage());
                channel.basicNack(tag, false, false);
            }
        } catch (Exception e) {
            log.error("Error inesperado en documento, enviado a DLQ: {}", e.getMessage());
            channel.basicNack(tag, false, false);
        }
    }

    private void procesar(DocumentoMensaje mensaje) {
        if (mensaje.productoId() == null || mensaje.nombre() == null || mensaje.nombre().isBlank()) {
            throw new IllegalArgumentException("Producto o nombre invalido");
        }
        // Simulacion de fallo temporal para la demo
        if (mensaje.productoId() == 888L) {
            throw new IllegalStateException("Servicio de documentos no disponible");
        }
        log.info("Documento {} generado para producto {} ({})",
                mensaje.tipo(), mensaje.productoId(), mensaje.nombre());
    }
}