package com.example.notification.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class KafkaConsumerConfigTest {

    @Test
    void createsTopicBasedRetryConfiguration() {
        KafkaConsumerConfig config = new KafkaConsumerConfig();
        ReflectionTestUtils.setField(config, "eventsTopic", "events");
        ReflectionTestUtils.setField(config, "retryTopicSuffix", ".retry");
        ReflectionTestUtils.setField(config, "dlqTopicSuffix", ".dlq");

        assertThat(config.eventsRetryTopicConfiguration(mock(KafkaTemplate.class))).isNotNull();
    }
}
