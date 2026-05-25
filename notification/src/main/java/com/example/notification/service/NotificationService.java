package com.example.notification.service;

import com.example.notification.event.EventCreatedMessage;
import com.example.notification.result.NotificationResultPublisher;
import com.example.notification.sms.SmsGatewayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationResultPublisher resultPublisher;
    private final SmsGatewayClient smsGatewayClient;

    public void sendNotification(EventCreatedMessage message) {
        smsGatewayClient.sendSms(message);
        resultPublisher.publishDelivered(message.eventId());
    }
}
