package com.example.notification.processed;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessedEventServiceTest {

    private final ProcessedEventRepository repository = mock(ProcessedEventRepository.class);
    private final ProcessedEventService service = new ProcessedEventService(repository);

    ProcessedEventServiceTest() {
        ReflectionTestUtils.setField(service, "processingTimeoutSeconds", 30L);
    }

    @Test
    void claimsNewEventWithAtomicInsert() {
        when(repository.insertProcessingIfAbsent(42L)).thenReturn(1);

        assertThat(service.tryStartProcessing(42L)).isEqualTo(ProcessingDecision.CLAIMED);
    }

    @Test
    void claimsFailedEventWithConditionalUpdate() {
        when(repository.insertProcessingIfAbsent(42L)).thenReturn(0);
        when(repository.restartFailedOrStaleProcessing(org.mockito.ArgumentMatchers.eq(42L), any(LocalDateTime.class)))
                .thenReturn(1);

        assertThat(service.tryStartProcessing(42L)).isEqualTo(ProcessingDecision.CLAIMED);
    }

    @Test
    void reportsEventClaimedByAnotherConsumerAsInProgress() {
        when(repository.insertProcessingIfAbsent(42L)).thenReturn(0);
        when(repository.restartFailedOrStaleProcessing(org.mockito.ArgumentMatchers.eq(42L), any(LocalDateTime.class)))
                .thenReturn(0);

        assertThat(service.tryStartProcessing(42L)).isEqualTo(ProcessingDecision.IN_PROGRESS);
        verify(repository).restartFailedOrStaleProcessing(
                org.mockito.ArgumentMatchers.eq(42L),
                any(LocalDateTime.class)
        );
    }

    @Test
    void reportsCompletedEventAsAlreadyProcessed() {
        ProcessedEvent event = new ProcessedEvent();
        event.setStatus(ProcessedEventStatus.PROCESSED);
        when(repository.findById(42L)).thenReturn(Optional.of(event));

        assertThat(service.tryStartProcessing(42L)).isEqualTo(ProcessingDecision.ALREADY_PROCESSED);
    }
}
