package com.cloudnative.tiendaperritos.kafka.admin;

import java.util.Map;

public record KafkaTopicSummary(
        String name,
        int partitions,
        int replicationFactor,
        Map<String, String> configurations) {
}
