package com.example.events.event.service;

import com.example.events.event.EventsMapper;
import com.example.events.event.EventsRepository;
import com.example.events.event.dto.EventRequest;
import com.example.events.event.dto.EventResponse;
import com.example.events.outbox.EventsOutboxService;
import com.example.events.outbox.OutboxMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventsServiceImpl implements EventsService {

    private final EventsRepository eventsRepository;
    private final EventsMapper eventsMapper;
    private final EventsOutboxService eventsOutboxService;
    private final OutboxMapper outboxMapper;

    @Override
    @Transactional
    public EventResponse sendMessage(EventRequest request) {
        var event = eventsMapper.toEvents(request);
        var savedEvent = eventsRepository.save(event);

        eventsOutboxService.saveEvent(outboxMapper.toOutbox(savedEvent));

        return eventsMapper.toEventResponse(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventsRepository.findAll().stream()
                .map(eventsMapper::toEventResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(Long eventId) {
        return eventsMapper.toEventResponse(eventsRepository.findById(eventId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Event not found with id: " + eventId)));
    }
}
