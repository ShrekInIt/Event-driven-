package com.example.events.outbox.dto;

public record OutboxMessage(
        String key,
        String payload
) {}
