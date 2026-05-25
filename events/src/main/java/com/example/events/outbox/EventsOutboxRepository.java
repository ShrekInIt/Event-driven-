package com.example.events.outbox;

import com.example.events.outbox.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventsOutboxRepository extends JpaRepository<EventsOutbox, Long> {
    List<EventsOutbox> findTop100ByStatusOrderByCreatedAtAsc(Status status);

    List<EventsOutbox> findTop100ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
            Status status,
            LocalDateTime now
    );

    List<EventsOutbox> findTop100ByStatusAndProcessingStartedAtLessThanEqualOrderByCreatedAtAsc(
            Status status,
            LocalDateTime timeout
    );
}
