package com.example.events.outbox.publisher;

import com.example.events.kafka.KafkaSender;
import com.example.events.outbox.EventsOutbox;
import com.example.events.outbox.EventsOutboxRepository;
import com.example.events.outbox.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxPublisherTest {

    private final EventsOutboxRepository repository = mock(EventsOutboxRepository.class);
    private final KafkaSender kafkaSender = mock(KafkaSender.class);
    private final OutboxPublisher publisher = new OutboxPublisher(repository, kafkaSender);

    @BeforeEach
    void setProperties() {
        ReflectionTestUtils.setField(publisher, "timeoutProcessingSeconds", 120L);
        ReflectionTestUtils.setField(publisher, "nextRetryDelaySeconds", 60L);
    }

    @Test
    void marksPublishedEventAsSent() throws Exception {
        EventsOutbox outbox = outboxWith(Status.PENDING, 0);
        when(repository.findTop100ByStatusOrderByCreatedAtAsc(Status.PENDING)).thenReturn(List.of(outbox));

        publisher.publishPendingEvents();

        verify(kafkaSender).sendToKafka(any());
        assertThat(outbox.getStatus()).isEqualTo(Status.SENT);
        assertThat(outbox.getProcessedAt()).isNotNull();
    }

    @Test
    void movesEventToDeadAfterFifthFailure() throws Exception {
        EventsOutbox outbox = outboxWith(Status.PENDING, 4);
        when(repository.findTop100ByStatusOrderByCreatedAtAsc(Status.PENDING)).thenReturn(List.of(outbox));
        doThrow(new RuntimeException("Kafka unavailable")).when(kafkaSender).sendToKafka(any());

        publisher.publishPendingEvents();

        assertThat(outbox.getStatus()).isEqualTo(Status.DEAD);
        assertThat(outbox.getRetry_count()).isEqualTo(5);
        assertThat(outbox.getNextRetryAt()).isNull();
        assertThat(outbox.getLast_error()).contains("Kafka unavailable");
    }

    private EventsOutbox outboxWith(Status status, int retryCount) {
        EventsOutbox outbox = new EventsOutbox();
        outbox.setId(10L);
        outbox.setPayload("{}");
        outbox.setTraceId("trace-123");
        outbox.setStatus(status);
        outbox.setRetry_count(retryCount);
        return outbox;
    }
}
