package com.example.events.result;

import com.example.events.event.service.EventsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationResultConsumer {

    private final ObjectMapper objectMapper;
    private final EventsService eventsService;

    @KafkaListener(
            topics = "${topics.results}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            String payload,
            @Header(value = "X-Trace-Id", required = false) String traceId,
            Acknowledgment acknowledgment
    ) throws JsonProcessingException
    {
        try {
            if (traceId != null && !traceId.isBlank()) {
                MDC.put("traceId", traceId);
            }
            NotificationResultMessage eventMessage = objectMapper.readValue(payload, NotificationResultMessage.class);
            log.info(
                    "Received event message: eventId={}, reason={}, processed={}, status={}",
                    eventMessage.eventId(),
                    eventMessage.reason(),
                    eventMessage.processedAt(),
                    eventMessage.status()
            );

            if(eventMessage.status().equals("DELIVERED")){
                eventsService.markAsDelivered(eventMessage.eventId());
            } else {
                eventsService.markAsFailed(eventMessage.eventId());
            }
            acknowledgment.acknowledge();
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize message: {}", payload, e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
