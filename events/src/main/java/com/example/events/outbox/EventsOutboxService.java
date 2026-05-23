package com.example.events.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventsOutboxService {
    private final EventsOutboxRepository eventsOutboxRepository;

    public void saveEvent(EventsOutbox event) {
        log.info("Event saved to outbox: {}", event);
        eventsOutboxRepository.save(event);
    }
}
