package com.example.events.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaTopicConfigTest {

    @Test
    void declaresMainRetryDlqAndResultTopics() {
        KafkaTopicConfig config = new KafkaTopicConfig();
        ReflectionTestUtils.setField(config, "eventsTopic", "events");
        ReflectionTestUtils.setField(config, "retryTopicSuffix", ".retry");
        ReflectionTestUtils.setField(config, "dlqTopicSuffix", ".dlq");
        ReflectionTestUtils.setField(config, "resultTopic", "notification-results");
        ReflectionTestUtils.setField(config, "resultRetryTopicSuffix", ".retry");
        ReflectionTestUtils.setField(config, "resultDlqTopicSuffix", ".dlq");

        assertThat(config.eventsTopic().name()).isEqualTo("events");
        assertThat(config.eventsRetryTopic().name()).isEqualTo("events.retry");
        assertThat(config.eventsDlqTopic().name()).isEqualTo("events.dlq");
        assertThat(config.resultTopic().name()).isEqualTo("notification-results");
        assertThat(config.resultRetryTopic().name()).isEqualTo("notification-results.retry");
        assertThat(config.resultDlqTopic().name()).isEqualTo("notification-results.dlq");
    }
}
