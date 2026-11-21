package com.cloudnative.tiendaperritos.kafka.admin;

import jakarta.validation.constraints.Min;
import java.util.Map;

public record UpdateKafkaTopicRequest(
        @Min(1) Integer partitions,
        Map<String, String> configurations) {
}
