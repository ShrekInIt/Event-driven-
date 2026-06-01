package com.example.events.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Value("${topics.events}")
    private String eventsTopic;

    @Value("${topics.events-dlq-suffix}")
    private String dlqTopicSuffix;

    @Value("${topics.events-retry-suffix}")
    private String retryTopicSuffix;

    @Value("${topics.results}")
    private String resultTopic;

    @Value("${topics.results-retry-suffix}")
    private String resultRetryTopicSuffix;

    @Value("${topics.results-dlq-suffix}")
    private String resultDlqTopicSuffix;

    @Bean
    public NewTopic eventsTopic() {
        return new NewTopic(eventsTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic eventsDlqTopic() {
        return new NewTopic(eventsTopic + dlqTopicSuffix, 1, (short) 1);
    }

    @Bean
    public NewTopic eventsRetryTopic() {
        return new NewTopic(eventsTopic + retryTopicSuffix, 1, (short) 1);
    }

    @Bean
    public NewTopic resultTopic() {
        return new NewTopic(resultTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic resultRetryTopic() {
        return new NewTopic(resultTopic + resultRetryTopicSuffix, 1, (short) 1);
    }

    @Bean
    public NewTopic resultDlqTopic() {
        return new NewTopic(resultTopic + resultDlqTopicSuffix, 1, (short) 1);
    }
}
