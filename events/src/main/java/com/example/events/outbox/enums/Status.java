package com.example.events.outbox.enums;

public enum Status {
    PENDING,
    SENT,
    PROCESSING,
    FAILED,
    DEAD
}
