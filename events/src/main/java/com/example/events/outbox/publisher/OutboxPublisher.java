package com.example.events.outbox.publisher;

import com.example.events.kafka.KafkaSender;
import com.example.events.outbox.EventsOutbox;
import com.example.events.outbox.EventsOutboxRepository;
import com.example.events.outbox.dto.OutboxMessage;
import com.example.events.outbox.enums.Status;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxPublisher {

    @Value("${outbox.publisher.processing-timeout-seconds}")
    private Long timeoutProcessingSeconds;

    @Value("${outbox.publisher.next-retry-delay-seconds}")
    private Long nextRetryDelaySeconds;

    private final EventsOutboxRepository outboxRepository;

    private final KafkaSender kafkaSender;

    @Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay-ms:5000}")
    public void publishPendingEvents(){
        List<EventsOutbox> pendingEvents =
                outboxRepository.findTop100ByStatusOrderByCreatedAtAsc(Status.PENDING);
        for(EventsOutbox eventsOutbox : pendingEvents){
            publishEvent(eventsOutbox);
        }
    }

    @Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay-ms:5000}")
    public void publishFailedEvents(){
        List<EventsOutbox> failedEvents =
                outboxRepository
                        .findTop100ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(Status.FAILED, LocalDateTime.now());
        for(EventsOutbox eventsOutbox : failedEvents){
            publishEvent(eventsOutbox);
        }
    }

    @Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay-ms:5000}")
    public void recoverStuckProcessingEvents(){
        List<EventsOutbox> failedEvents =
                outboxRepository
                        .findTop100ByStatusAndProcessingStartedAtLessThanEqualOrderByCreatedAtAsc(
                                Status.PROCESSING,
                                LocalDateTime.now().minusSeconds(timeoutProcessingSeconds)
                        );
        for(EventsOutbox eventsOutbox : failedEvents){
            markAsFailed(eventsOutbox,"Processing timeout exceeded");
        }
    }

    private void publishEvent(EventsOutbox eventsOutbox) {
        markAsProcessing(eventsOutbox);
        try {
            kafkaSender.sendToKafka(toOutboxMessage(eventsOutbox));
            markAsSent(eventsOutbox);
        } catch (CallNotPermittedException ex) {
            postponeDueToCircuitBreaker(eventsOutbox);
        } catch (Exception ex) {
            markAsFailed(eventsOutbox, ex);
        }
    }

    private void postponeDueToCircuitBreaker(EventsOutbox eventsOutbox) {
        eventsOutbox.setStatus(Status.FAILED);
        eventsOutbox.setLast_error("Kafka circuit breaker is open");
        eventsOutbox.setNextRetryAt(LocalDateTime.now().plusSeconds(nextRetryDelaySeconds));

        outboxRepository.save(eventsOutbox);

        log.warn(
                "Outbox event id={} postponed because Kafka circuit breaker is open. Next retry at={}",
                eventsOutbox.getId(),
                eventsOutbox.getNextRetryAt()
        );
    }

    private void markAsProcessing(EventsOutbox eventsOutbox) {
        eventsOutbox.setStatus(Status.PROCESSING);
        eventsOutbox.setProcessingStartedAt(LocalDateTime.now());
        outboxRepository.save(eventsOutbox);
        log.info("Outbox event id={} marked as PROCESSING", eventsOutbox.getId());
    }

    private void markAsFailed(EventsOutbox eventsOutbox, String reason) {
        int nextRetryCount = eventsOutbox.getRetry_count() + 1;

        if (nextRetryCount >= 5) {
            eventsOutbox.setStatus(Status.DEAD);
            eventsOutbox.setRetry_count(nextRetryCount);
            eventsOutbox.setLast_error("Max retry attempts reached. Last error: " + reason);
            eventsOutbox.setNextRetryAt(null);

            outboxRepository.save(eventsOutbox);

            log.error(
                    "Outbox event id={} moved to DEAD after {} retries. Reason: {}",
                    eventsOutbox.getId(),
                    nextRetryCount,
                    reason
            );
            return;
        }

        eventsOutbox.setStatus(Status.FAILED);
        eventsOutbox.setRetry_count(nextRetryCount);
        eventsOutbox.setLast_error(reason);
        eventsOutbox.setNextRetryAt(LocalDateTime.now().plusSeconds(nextRetryDelaySeconds));

        outboxRepository.save(eventsOutbox);

        log.warn(
                "Outbox event id={} marked as FAILED. Retry count={}. Next retry at={}. Reason: {}",
                eventsOutbox.getId(),
                nextRetryCount,
                eventsOutbox.getNextRetryAt(),
                reason
        );
    }

    private void markAsFailed(EventsOutbox eventsOutbox, Throwable ex) {
        String reason = ex.getMessage() != null
                ? ex.getMessage()
                : ex.getClass().getSimpleName();

        markAsFailed(eventsOutbox, reason);

        log.error("Kafka publish failed for outbox event id={}", eventsOutbox.getId(), ex);
    }

    private void markAsSent(EventsOutbox eventsOutbox) {
        eventsOutbox.setStatus(Status.SENT);
        eventsOutbox.setProcessedAt(LocalDateTime.now());
        outboxRepository.save(eventsOutbox);
        log.info("Successfully published event with id={}", eventsOutbox.getId());
    }

    private OutboxMessage toOutboxMessage(EventsOutbox eventsOutbox) {
        return new OutboxMessage(
                eventsOutbox.getId().toString(),
                eventsOutbox.getPayload()
        );
    }
}
