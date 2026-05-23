package com.example.events.event.dto;

import jakarta.annotation.Nullable;

public record EventRequest(
        @Nullable
        String text
) {}
