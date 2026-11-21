package com.cloudnative.tiendaperritos.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlingConfig {
    private static final Logger log = LoggerFactory.getLogger(KafkaErrorHandlingConfig.class);

    @Bean
    DefaultErrorHandler kafkaDefaultErrorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate,
            @Value("${app.kafka.topics.product-events-dlt}") String dltTopic) {
        DeadLetterPublishingRecoverer publisher = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (ConsumerRecord<?, ?> record, Exception exception) ->
                        new TopicPartition(dltTopic, record.partition()));
        ConsumerRecordRecoverer recoverer = (record, exception) -> {
            log.error("Evento enviado a DLT tras agotar reintentos: topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset(), exception);
            publisher.accept(record, exception);
        };
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));
        handler.addNotRetryableExceptions(IllegalArgumentException.class);
        return handler;
    }
}
