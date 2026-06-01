package com.example.notification.sms;

import com.example.notification.event.EventCreatedMessage;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmsGatewayClientRateLimiterTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    AopAutoConfiguration.class,
                    RateLimiterAutoConfiguration.class
            ))
            .withBean(SmsGatewayClient.class)
            .withPropertyValues(
                    "resilience4j.ratelimiter.instances.smsGateway.limit-for-period=1",
                    "resilience4j.ratelimiter.instances.smsGateway.limit-refresh-period=1m",
                    "resilience4j.ratelimiter.instances.smsGateway.timeout-duration=0"
            );

    @Test
    void rejectsCallsAboveConfiguredSmsGatewayLimit() {
        contextRunner.run(context -> {
            SmsGatewayClient client = context.getBean(SmsGatewayClient.class);
            EventCreatedMessage message = new EventCreatedMessage(42L, "message", "PENDING", null);

            client.sendSms(message);

            assertThatThrownBy(() -> client.sendSms(message))
                    .isInstanceOf(RequestNotPermitted.class);
        });
    }
}
