package com.example.notification.kafka;

public record KafkaMessage(
        String key,
        String payload
) {}
