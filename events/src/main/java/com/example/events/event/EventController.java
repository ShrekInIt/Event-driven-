package com.example.events.event;

import com.example.events.event.dto.EventRequest;
import com.example.events.event.dto.EventResponse;
import com.example.events.event.service.EventsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {
    private final EventsService eventService;

    @PostMapping("/send")
    public ResponseEntity<EventResponse> generateEvents(
            @RequestBody @Valid EventRequest request
    ) {
        log.info("Received event request: {}", request);
        EventResponse response = eventService.sendMessage(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable Long eventId
    ) {
        log.info("Received request to get event by ID: {}", eventId);
        EventResponse response = eventService.getEventById(eventId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        log.info("Received request to get all events");
        List<EventResponse> response = eventService.getAllEvents();
        return ResponseEntity.ok(response);
    }
}
