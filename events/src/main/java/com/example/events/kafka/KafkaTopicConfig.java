package com.example.events.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Value("${topics.events}")
    private String eventsTopic;

    @Value("${topics.events-dlq}")
    private String dlqTopic;

    @Value("${topics.results}")
    private String resultTopic;

    @Bean
    public NewTopic eventsTopic() {
        return new NewTopic(eventsTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic eventsDlqTopic() {
        return new NewTopic(dlqTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic resultTopic() {
        return new NewTopic(resultTopic, 1, (short) 1);
    }
}
