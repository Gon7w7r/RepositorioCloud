package com.cloudnative.tiendaperritos.kafka.consumidor;

import com.cloudnative.tiendaperritos.kafka.eventos.ProductoEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductoEventoConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProductoEventoConsumer.class);

    @KafkaListener(
            topics = "${app.kafka.topics.product-events}",
            groupId = "tienda-productos-auditoria")
    public void consumir(ProductoEvento evento) {
        if (evento == null || evento.eventoId() == null || evento.productoId() == null) {
            throw new IllegalArgumentException("El evento de producto no contiene identificadores obligatorios");
        }
        log.info("Evento de producto procesado: tipo={}, productoId={}, usuario={}, fecha={}",
                evento.tipo(), evento.productoId(), evento.usuario(), evento.fechaEvento());
    }
}
