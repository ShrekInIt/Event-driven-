package com.example.notification.processed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessedEventService {

    private final ProcessedEventRepository processedEventRepository;

    public boolean tryStartProcessing(Long eventId){
        Optional<ProcessedEvent> existingEvent = processedEventRepository.findById(eventId);

        if (existingEvent.isEmpty()){
            ProcessedEvent event = new ProcessedEvent();
            event.setEventId(eventId);
            event.setStatus(ProcessedEventStatus.PROCESSING);
            event.setProcessedAt(LocalDateTime.now());
            processedEventRepository.save(event);
            return true;
        }

        ProcessedEvent event = existingEvent.get();


        switch (event.getStatus()){
            case PROCESSED -> {
                log.info("Event with id {} has already been processed", eventId);
                return false;
            }
            case PROCESSING -> {
                log.info("Event with id {} is currently being processed by another instance", eventId);
                return false;
            }
            case FAILED -> {
                log.info("Event with id {} has previously failed processing. Retrying...", eventId);
                event.setStatus(ProcessedEventStatus.PROCESSING);
                event.setErrorMessage(null);
                event.setProcessedAt(LocalDateTime.now());
                processedEventRepository.save(event);
                return true;
            }
        }

        return false;
    }

    public void markAsProcessed(Long eventId){
        ProcessedEvent event = getProcessedEvent(eventId);
        event.setStatus(ProcessedEventStatus.PROCESSED);
        event.setProcessedAt(java.time.LocalDateTime.now());
        processedEventRepository.save(event);
        log.info("Marked event with id {} as processed", eventId);
    }

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
