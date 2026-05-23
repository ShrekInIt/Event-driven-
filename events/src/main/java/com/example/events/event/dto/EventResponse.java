package com.example.events.event.dto;

import com.example.events.event.StatusEvent;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record EventResponse(
        Long id,

        StatusEvent statusEvent,

        String text,

        @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss")
        LocalDateTime updatedAt
) {}
