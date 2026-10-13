package com.cloudnative.tiendaperritos.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HelloQueueConfig {

    public static final String HELLO_QUEUE = "hello";

    @Bean
    public Queue helloQueue() {
        return new Queue(HELLO_QUEUE, true);
    }
}