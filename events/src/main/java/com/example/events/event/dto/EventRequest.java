package com.example.events.event.dto;

import jakarta.validation.constraints.NotBlank;

public record EventRequest(
        @NotBlank
        String text
) {}
