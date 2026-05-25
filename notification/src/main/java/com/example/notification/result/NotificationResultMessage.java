package com.example.notification.result;

import java.time.LocalDateTime;

public record NotificationResultMessage(
        Long eventId,
        String status,
        String reason,
        LocalDateTime processedAt
) {}
