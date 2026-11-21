package com.cloudnative.tiendaperritos.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    private static final int PARTITIONS = 3;
    private static final short REPLICATION_FACTOR = 2;

    @Bean
    NewTopic productoEventosTopic(
            @Value("${app.kafka.topics.product-events}") String topic) {
        return TopicBuilder.name(topic)
                .partitions(PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .config("cleanup.policy", "delete")
                .config("retention.ms", "86400000")
                .config("segment.bytes", "1048576")
                .build();
    }

    @Bean
    NewTopic alertasStockTopic(
            @Value("${app.kafka.topics.stock-alerts}") String topic) {
        return TopicBuilder.name(topic)
                .partitions(PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .config("cleanup.policy", "delete")
                .config("retention.ms", "604800000")
                .config("segment.bytes", "1048576")
                .build();
    }

    @Bean
    NewTopic productoEventosDltTopic(
            @Value("${app.kafka.topics.product-events-dlt}") String topic) {
        return TopicBuilder.name(topic)
                .partitions(PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .config("cleanup.policy", "delete")
                .config("retention.ms", "604800000")
                .build();
    }
}
