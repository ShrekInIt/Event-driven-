package com.example.events.outbox;

import com.example.events.event.Events;
import com.example.events.event.StatusEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxMapperTest {

    private final OutboxMapper mapper = new OutboxMapper(new ObjectMapper().findAndRegisterModules());

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void storesTraceIdAndSerializedEventPayload() {
        Events event = new Events(
                42L,
                StatusEvent.PENDING,
                "message",
                LocalDateTime.of(2026, 6, 1, 12, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0)
        );
        MDC.put("traceId", "trace-123");

        EventsOutbox outbox = mapper.toOutbox(event);

        assertThat(outbox.getTraceId()).isEqualTo("trace-123");
        assertThat(outbox.getPayload()).contains("\"eventId\":42", "\"text\":\"message\"");
    }
}
