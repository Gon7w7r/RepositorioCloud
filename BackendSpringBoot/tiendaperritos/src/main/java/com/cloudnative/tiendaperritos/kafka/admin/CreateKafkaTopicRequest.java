package com.cloudnative.tiendaperritos.kafka.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Map;

public record CreateKafkaTopicRequest(
        @NotBlank
        @Pattern(regexp = "[A-Za-z0-9._-]+", message = "El nombre contiene caracteres no permitidos")
        String name,
        @Min(1) @Max(64) int partitions,
        @Min(1) @Max(3) int replicationFactor,
        Map<String, String> configurations) {
}
