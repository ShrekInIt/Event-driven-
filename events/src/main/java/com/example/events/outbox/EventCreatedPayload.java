package com.example.events.outbox;

import com.example.events.event.StatusEvent;

import java.time.LocalDateTime;

public record EventCreatedPayload(
        Long eventId,
        String text,
        StatusEvent statusEvent,
        LocalDateTime createdAt
) {}
