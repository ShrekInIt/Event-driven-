package com.example.notification.kafka;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaSender {

    @Value("${topics.results}")
    private String topic;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @CircuitBreaker(name = "kafkaPublisher")
    public void sendToKafka(KafkaMessage message) throws Exception {
        ProducerRecord<String, String> record =
                new ProducerRecord<>(topic, message.key(), message.payload());

        String traceId = MDC.get("traceId");
        if (traceId != null && !traceId.isBlank()) {
            record.headers().add(
                    "X-Trace-Id",
                    traceId.getBytes(StandardCharsets.UTF_8)
            );
        }

        kafkaTemplate.send(record).get();

        log.info("Sent message to Kafka: key={}, payload={}", message.key(), message.payload());
    }
}
