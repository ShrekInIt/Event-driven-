package com.example.notification.consumer;

import com.example.notification.processed.ProcessedEventService;
import com.example.notification.processed.ProcessingDecision;
import com.example.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.kafka.support.Acknowledgment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventNotificationConsumerTest {

    private final ProcessedEventService processedEventService = mock(ProcessedEventService.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final EventNotificationConsumer consumer = new EventNotificationConsumer(
            new ObjectMapper().findAndRegisterModules(),
            processedEventService,
            notificationService
    );
    private final Acknowledgment acknowledgment = mock(Acknowledgment.class);

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void acknowledgesDuplicateWithoutCallingGateway() throws Exception {
        when(processedEventService.tryStartProcessing(42L)).thenReturn(ProcessingDecision.ALREADY_PROCESSED);

        consumer.consume(payload(), "trace-123", acknowledgment);

        verify(notificationService, never()).sendNotification(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void marksSuccessfulNotificationAsProcessedAndAcknowledges() throws Exception {
        when(processedEventService.tryStartProcessing(42L)).thenReturn(ProcessingDecision.CLAIMED);

        consumer.consume(payload(), "trace-123", acknowledgment);

        verify(notificationService).sendNotification(any());
        verify(processedEventService).markAsProcessed(42L);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void marksFailedNotificationAndLeavesMessageUnacknowledgedForRetry() {
        when(processedEventService.tryStartProcessing(42L)).thenReturn(ProcessingDecision.CLAIMED);
        doThrow(new RuntimeException("SMS unavailable")).when(notificationService).sendNotification(any());

        assertThatThrownBy(() -> consumer.consume(payload(), "trace-123", acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("SMS unavailable");

        verify(processedEventService).markAsFailed(42L, "SMS unavailable");
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    void leavesInProgressEventUnacknowledgedForRetry() {
        when(processedEventService.tryStartProcessing(42L)).thenReturn(ProcessingDecision.IN_PROGRESS);

        assertThatThrownBy(() -> consumer.consume(payload(), "trace-123", acknowledgment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already being processed");

        verify(notificationService, never()).sendNotification(any());
        verify(acknowledgment, never()).acknowledge();
    }

    private String payload() {
        return """
                {"eventId":42,"text":"message","statusEvent":"PENDING","createdAt":"2026-06-01T12:00:00"}
                """;
    }
}
