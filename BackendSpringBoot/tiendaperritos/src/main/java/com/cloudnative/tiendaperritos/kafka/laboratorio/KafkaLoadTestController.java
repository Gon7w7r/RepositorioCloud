package com.cloudnative.tiendaperritos.kafka.laboratorio;

import com.cloudnative.tiendaperritos.kafka.eventos.ProductoEvento;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafka/load-test")
public class KafkaLoadTestController {

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final String topic;

    public KafkaLoadTestController(
            KafkaTemplate<Object, Object> kafkaTemplate,
            @Value("${app.kafka.topics.product-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @PostMapping("/start")
    public ResponseEntity<CargaKafkaResult> iniciar(@RequestParam(defaultValue = "100000") int count)
            throws InterruptedException {
        if (count < 1 || count > 500000) {
            return ResponseEntity.badRequest().body(
                    new CargaKafkaResult(0, 0, 0, 0, 0, 0,
                            "count debe estar entre 1 y 500000"));
        }

        CountDownLatch acknowledgements = new CountDownLatch(count);
        AtomicLong failures = new AtomicLong();
        long start = System.nanoTime();

        for (int i = 0; i < count; i++) {
            ProductoEvento event = new ProductoEvento(
                    UUID.randomUUID().toString(),
                    "PRUEBA_CARGA",
                    (long) i + 1,
                    "Producto de carga " + i,
                    BigDecimal.ONE,
                    20,
                    Instant.now().toString(),
                    "prueba-carga");
            kafkaTemplate.send(topic, "carga-" + i, event).whenComplete((result, error) -> {
                if (error != null) {
                    failures.incrementAndGet();
                }
                acknowledgements.countDown();
            });
        }

        boolean completed = acknowledgements.await(60, TimeUnit.SECONDS);
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        long pending = acknowledgements.getCount();
        long acknowledged = count - failures.get() - pending;
        double throughput = elapsedMs == 0 ? acknowledged : acknowledged * 1000.0 / elapsedMs;
        String message = completed ? "Todos los envíos recibieron confirmación del broker"
                : "La espera terminó antes de recibir todas las confirmaciones";

        return ResponseEntity.ok(new CargaKafkaResult(
                count, acknowledged, failures.get(), pending, elapsedMs, throughput, message));
    }

    public record CargaKafkaResult(
            long enviados,
            long confirmados,
            long fallidos,
            long pendientes,
            long tiempoMs,
            double throughputMsgPorSegundo,
            String resultado) {
    }
}
