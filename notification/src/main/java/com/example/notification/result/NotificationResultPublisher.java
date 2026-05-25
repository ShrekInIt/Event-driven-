package com.example.notification.result;

import com.example.notification.kafka.KafkaMessage;
import com.example.notification.kafka.KafkaSender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationResultPublisher {

    private final ObjectMapper objectMapper;
    private final KafkaSender kafkaSender;

    public void publishDelivered(Long eventId){
        NotificationResultMessage message = new NotificationResultMessage(
                eventId,
                "DELIVERED",
                null,
                LocalDateTime.now()
        );
        try {
            kafkaSender.sendToKafka(toKafkaMessage(message));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private KafkaMessage toKafkaMessage(NotificationResultMessage message) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return new KafkaMessage(
                message.eventId().toString(),
                payload
        );
    }
}
