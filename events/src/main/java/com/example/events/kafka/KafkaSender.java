package com.example.events.kafka;

import com.example.events.outbox.dto.OutboxMessage;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaSender {

    @Value("${outbox.topic}")
    private String topic;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @CircuitBreaker(name = "kafkaPublisher")
    public void sendToKafka(OutboxMessage message) throws Exception {
        log.info("Sending outbox event id={} to Kafka topic={}", message.key(), topic);

        kafkaTemplate.send(topic, message.key(), message.payload()).get();

        log.info("Outbox event id={} successfully sent to Kafka topic={}", message.key(), topic);
    }
}
