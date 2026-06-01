package com.example.events.result;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ResultKafkaConsumerConfigTest {

    @Test
    void createsTopicBasedRetryConfiguration() {
        ResultKafkaConsumerConfig config = new ResultKafkaConsumerConfig();
        ReflectionTestUtils.setField(config, "resultsTopic", "notification-results");
        ReflectionTestUtils.setField(config, "retryTopicSuffix", ".retry");
        ReflectionTestUtils.setField(config, "dlqTopicSuffix", ".dlq");

        assertThat(config.resultsRetryTopicConfiguration(mock(KafkaTemplate.class))).isNotNull();
    }
}
