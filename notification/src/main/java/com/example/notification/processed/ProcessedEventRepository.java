package com.example.notification.processed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    @Modifying
    @Query(value = """
            INSERT INTO processed_events(event_id, status, processed_at)
            VALUES (:eventId, 'PROCESSING', CURRENT_TIMESTAMP)
            ON CONFLICT (event_id) DO NOTHING
            """, nativeQuery = true)
    int insertProcessingIfAbsent(@Param("eventId") Long eventId);

    @Modifying
    @Query(value = """
            UPDATE processed_events
            SET status = 'PROCESSING',
                error_message = NULL,
                processed_at = CURRENT_TIMESTAMP
            WHERE event_id = :eventId
              AND (
                  status = 'FAILED'
                  OR (status = 'PROCESSING' AND processed_at <= :staleBefore)
              )
            """, nativeQuery = true)
    int restartFailedOrStaleProcessing(
            @Param("eventId") Long eventId,
            @Param("staleBefore") LocalDateTime staleBefore
    );
}
