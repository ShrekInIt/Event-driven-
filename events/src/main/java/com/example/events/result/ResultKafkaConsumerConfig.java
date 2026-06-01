package com.example.events.result;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;

@Configuration
public class ResultKafkaConsumerConfig {

    @Value("${topics.results}")
    private String resultsTopic;

    @Value("${topics.results-retry-suffix}")
    private String retryTopicSuffix;

    @Value("${topics.results-dlq-suffix}")
    private String dlqTopicSuffix;

    @Bean
    public RetryTopicConfiguration resultsRetryTopicConfiguration(
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        return RetryTopicConfigurationBuilder
                .newInstance()
                .includeTopic(resultsTopic)
                .fixedBackOff(1000L)
                .maxAttempts(4)
                .useSingleTopicForSameIntervals()
                .retryTopicSuffix(retryTopicSuffix)
                .dltSuffix(dlqTopicSuffix)
                .doNotAutoCreateRetryTopics()
                .create(kafkaTemplate);
    }
}
