package com.example.notification.processed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessedEventService {

    private final ProcessedEventRepository processedEventRepository;

    @Value("${processed-events.processing-timeout-seconds:30}")
    private long processingTimeoutSeconds;

    @Transactional
    public ProcessingDecision tryStartProcessing(Long eventId){
        if (processedEventRepository.insertProcessingIfAbsent(eventId) == 1) {
            return ProcessingDecision.CLAIMED;
        }

        LocalDateTime staleBefore = LocalDateTime.now().minusSeconds(processingTimeoutSeconds);
        if (processedEventRepository.restartFailedOrStaleProcessing(eventId, staleBefore) == 1) {
            log.info("Event with id {} was failed or stale. Retrying...", eventId);
            return ProcessingDecision.CLAIMED;
        }

        return processedEventRepository.findById(eventId)
                .filter(event -> event.getStatus() == ProcessedEventStatus.PROCESSED)
                .map(event -> ProcessingDecision.ALREADY_PROCESSED)
                .orElse(ProcessingDecision.IN_PROGRESS);
    }

    @Transactional
    public void markAsProcessed(Long eventId){
        ProcessedEvent event = getProcessedEvent(eventId);
        event.setStatus(ProcessedEventStatus.PROCESSED);
        event.setProcessedAt(java.time.LocalDateTime.now());
        processedEventRepository.save(event);
        log.info("Marked event with id {} as processed", eventId);
    }

    @Transactional
    public void markAsFailed(Long eventId, String errorMessage){
        ProcessedEvent event = getProcessedEvent(eventId);
        event.setStatus(ProcessedEventStatus.FAILED);
        event.setErrorMessage(errorMessage);
        event.setProcessedAt(java.time.LocalDateTime.now());
        processedEventRepository.save(event);
        log.info("Marked event with id {} as failed with error: {}", eventId, errorMessage);
    }

    private ProcessedEvent getProcessedEvent(Long eventId){
        return processedEventRepository
                .findById(eventId)
                .orElseThrow(() -> new IllegalStateException("Event with id " + eventId + " not found"));
    }
}
