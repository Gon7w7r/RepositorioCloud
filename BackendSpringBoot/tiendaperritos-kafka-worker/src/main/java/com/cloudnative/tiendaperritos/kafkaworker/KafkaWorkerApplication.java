package com.cloudnative.tiendaperritos.kafkaworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication(scanBasePackages = "com.cloudnative.tiendaperritos")
public class KafkaWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaWorkerApplication.class, args);
    }
}
