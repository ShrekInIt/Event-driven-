package com.example.notification.consumer;

import com.example.notification.event.EventCreatedMessage;
import com.example.notification.processed.ProcessedEventService;
import com.example.notification.service.NotificationService;
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
@Slf4j
@RequiredArgsConstructor
public class EventNotificationConsumer {

    private final ObjectMapper objectMapper;
    private final ProcessedEventService processedEventService;
    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${topics.events}",
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
            EventCreatedMessage eventMessage = objectMapper.readValue(payload, EventCreatedMessage.class);
            log.info(
                    "Received event message: eventId={}, text={}, statusEvent={}, createdAt={}",
                    eventMessage.eventId(),
                    eventMessage.text(),
                    eventMessage.statusEvent(),
                    eventMessage.createdAt()
            );

            if (!processedEventService.tryStartProcessing(eventMessage.eventId())) {
                log.info("Duplicate event received. eventId={}", eventMessage.eventId());
                acknowledgment.acknowledge();
                return;
            }

            try {
                notificationService.sendNotification(eventMessage);
                processedEventService.markAsProcessed(eventMessage.eventId());
                acknowledgment.acknowledge();
            }catch (Exception e){
                processedEventService.markAsFailed(eventMessage.eventId(), e.getMessage());
                throw e;
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize message: {}", payload, e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
