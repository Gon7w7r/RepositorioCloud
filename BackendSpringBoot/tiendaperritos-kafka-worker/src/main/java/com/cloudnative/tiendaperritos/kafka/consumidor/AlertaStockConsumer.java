package com.cloudnative.tiendaperritos.kafka.consumidor;

import com.cloudnative.tiendaperritos.kafka.eventos.AlertaStock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AlertaStockConsumer {
    private static final Logger log = LoggerFactory.getLogger(AlertaStockConsumer.class);

    @KafkaListener(topics = "${app.kafka.topics.stock-alerts}", groupId = "tienda-alertas-consumer")
    public void consumir(AlertaStock alerta) {
        if (alerta == null || alerta.productoId() == null) {
            throw new IllegalArgumentException("La alerta de stock no contiene el producto asociado");
        }
        log.warn("Worker recibió alerta de stock: productoId={}, nombre={}, stock={}, severidad={}",
                alerta.productoId(), alerta.nombre(), alerta.stockDisponible(), alerta.severidad());
    }
}
