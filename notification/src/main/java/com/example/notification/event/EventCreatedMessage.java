package com.example.notification.event;

import java.time.LocalDateTime;

public record EventCreatedMessage(
        Long eventId,
        String text,
        String statusEvent,
        LocalDateTime createdAt
) {}
