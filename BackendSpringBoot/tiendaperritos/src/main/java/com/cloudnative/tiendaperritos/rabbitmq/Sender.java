package com.cloudnative.tiendaperritos.rabbitmq;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class Sender {

    private final RabbitTemplate rabbitTemplate;

    public Sender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(String message) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String fullMessage = "[" + timestamp + "] " + message;

        rabbitTemplate.convertAndSend(HelloQueueConfig.HELLO_QUEUE, fullMessage);
        System.out.println("[OK] Mensaje enviado: " + fullMessage);
    }
}