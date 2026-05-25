package com.example.notification.sms;

import com.example.notification.result.NotificationResultPublisher;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import com.example.notification.event.EventCreatedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SmsGatewayClient {

    private final NotificationResultPublisher notificationResultPublisher;

    @RateLimiter(name = "smsGateway")
    public void sendSms(EventCreatedMessage message) {
        log.info("Sending notification for eventId={}, text={}", message.eventId(), message.text());

    }
}
