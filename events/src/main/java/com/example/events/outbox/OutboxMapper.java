package com.example.events.outbox;

import com.example.events.event.Events;
import com.example.events.outbox.enums.EventType;
import com.example.events.outbox.enums.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxMapper {
    private final ObjectMapper objectMapper;

    public EventsOutbox toOutbox(Events event) {
        EventsOutbox outbox = new EventsOutbox();

        outbox.setEventType(EventType.EVENT_CREATED);
        outbox.setPayload(toPayload(event));
        outbox.setStatus(Status.PENDING);
        outbox.setTraceId(MDC.get("traceId"));

        return outbox;
    }

    private String toPayload(Events event) {
        try {
            var payload = new EventCreatedPayload(
                    event.getId(),
                    event.getText(),
                    event.getStatusEvent(),
                    event.getCreatedAt()
            );

            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize outbox payload", ex);
        }
    }
}
