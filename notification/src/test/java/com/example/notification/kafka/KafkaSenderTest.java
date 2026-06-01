package com.example.notification.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaSenderTest {

    private final KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
    private final KafkaSender sender = new KafkaSender(kafkaTemplate);

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void addsTraceIdHeaderFromMdc() throws Exception {
        ReflectionTestUtils.setField(sender, "topic", "notification-results");
        CompletableFuture<SendResult<String, String>> result = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(org.mockito.ArgumentMatchers.<ProducerRecord<String, String>>any()))
                .thenReturn(result);
        MDC.put("traceId", "trace-123");

        sender.sendToKafka(new KafkaMessage("42", "{}"));

        ArgumentCaptor<ProducerRecord<String, String>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());
        assertThat(new String(
                captor.getValue().headers().lastHeader("X-Trace-Id").value(),
                StandardCharsets.UTF_8
        )).isEqualTo("trace-123");
    }
}
