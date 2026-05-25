package com.example.events.kafka;

import com.example.events.outbox.dto.OutboxMessage;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaSender {

    @Value("${topics.events}")
    private String topic;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @CircuitBreaker(name = "kafkaPublisher")
    public void sendToKafka(OutboxMessage message) throws Exception {
        ProducerRecord<String, String> record =
                new ProducerRecord<>(topic, message.key(), message.payload());

        if (message.traceId() != null && !message.traceId().isBlank()) {
            record.headers().add(
                    "X-Trace-Id",
                    message.traceId().getBytes(StandardCharsets.UTF_8)
            );
        }

        kafkaTemplate.send(record).get();
    }
}
