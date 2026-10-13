package com.cloudnative.tiendaperritos.rabbitmq;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class Receiver {

    @RabbitListener(queues = HelloQueueConfig.HELLO_QUEUE)
    public void receiveMessage(String message) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        System.out.println("[" + timestamp + "] [OK] Mensaje recibido: " + message);
    }
}