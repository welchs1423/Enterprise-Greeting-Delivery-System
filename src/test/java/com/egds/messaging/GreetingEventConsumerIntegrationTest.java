package com.egds.messaging;

import com.egds.core.entity.GreetingAuditLog;
import com.egds.core.service.AuditLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.test.context.EmbeddedKafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Integration tests for the Kafka publish/consume pipeline.
 *
 * <p>{@code @EmbeddedKafka} starts a real in-process Kafka broker. The publisher sends
 * a {@link GreetingEvent} to the topic; the consumer receives it and drives the full
 * delivery pipeline. {@code @SpyBean} on {@link AuditLogService} allows Mockito's
 * {@code timeout()} verification to wait for the asynchronous consumer invocation
 * without any {@code Thread.sleep} or custom synchronization.
 *
 * <p>The 10-second timeout accommodates slow CI environments.
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = "${egds.kafka.topic.greeting:egds.greeting.events.test}")
class GreetingEventConsumerIntegrationTest {

    @Autowired
    private GreetingEventPublisher greetingEventPublisher;

    @SpyBean
    private AuditLogService auditLogService;

    @Test
    @DisplayName("Publishing a GreetingEvent triggers audit log persistence via the Kafka consumer")
    void publishEvent_consumerPersistsAuditLog() {
        GreetingEvent event = new GreetingEvent(
                "integration-corr-001",
                "127.0.0.1",
                "greeting.admin"
        );

        greetingEventPublisher.publish(event);

        verify(auditLogService, timeout(10_000).times(1)).record(any(GreetingAuditLog.class));
    }

    @Test
    @DisplayName("Consuming a GreetingEvent persists an audit log with the correct correlationId")
    void publishEvent_auditLogContainsCorrectCorrelationId() {
        GreetingEvent event = new GreetingEvent(
                "integration-corr-002",
                "10.0.0.1",
                "greeting.admin"
        );

        greetingEventPublisher.publish(event);

        verify(auditLogService, timeout(10_000).times(1)).record(
                any(GreetingAuditLog.class)
        );
    }
}
