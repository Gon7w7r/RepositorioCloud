package com.cloudnative.tiendaperritos.kafka.admin;

import java.util.Set;

public record KafkaConsumerGroupSummary(
        String groupId,
        String state,
        int memberCount,
        Set<String> topics) {
}
