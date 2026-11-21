package com.cloudnative.tiendaperritos.kafka.laboratorio;

import com.cloudnative.tiendaperritos.kafka.eventos.ProductoEvento;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka-lab")
public class ConsumidorLentoKafka {

    private static final Logger log = LoggerFactory.getLogger(ConsumidorLentoKafka.class);
    private final AtomicLong procesados = new AtomicLong();
    private final long delayMs;

    public ConsumidorLentoKafka(@Value("${app.kafka.lab.consumer-delay-ms:10}") long delayMs) {
        this.delayMs = delayMs;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.product-events}",
            groupId = "tienda-kafka-lab-consumidor-lento")
    public void consumir(ProductoEvento evento) throws InterruptedException {
        Thread.sleep(delayMs);
        long total = procesados.incrementAndGet();
        if (total % 1000 == 0) {
            log.info("Consumidor lento procesó {} eventos; último productoId={}", total, evento.productoId());
        }
    }
}
