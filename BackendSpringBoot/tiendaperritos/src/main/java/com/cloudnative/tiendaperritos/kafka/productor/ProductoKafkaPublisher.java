package com.cloudnative.tiendaperritos.kafka.productor;

import com.cloudnative.tiendaperritos.kafka.eventos.ProductoEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductoKafkaPublisher {

    private static final Logger log = LoggerFactory.getLogger(ProductoKafkaPublisher.class);
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final String topic;

    public ProductoKafkaPublisher(
            KafkaTemplate<Object, Object> kafkaTemplate,
            @Value("${app.kafka.topics.product-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publicar(ProductoEvento evento) {
        String key = evento.productoId() == null ? evento.eventoId() : evento.productoId().toString();
        kafkaTemplate.send(topic, key, evento).whenComplete((resultado, error) -> {
            if (error != null) {
                log.error("No se pudo publicar el evento {} en Kafka", evento.eventoId(), error);
                return;
            }
            log.info("Evento Kafka publicado: topic={}, particion={}, offset={}, productoId={}",
                    resultado.getRecordMetadata().topic(),
                    resultado.getRecordMetadata().partition(),
                    resultado.getRecordMetadata().offset(),
                    evento.productoId());
        });
    }
}
