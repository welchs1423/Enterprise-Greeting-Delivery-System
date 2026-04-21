package com.egds.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GreetingEventPublisher}.
 * No Spring context is loaded; {@link KafkaTemplate} is mocked via Mockito to isolate
 * publisher logic from Kafka broker connectivity.
 */
@ExtendWith(MockitoExtension.class)
class GreetingEventPublisherTest {

    private static final String TOPIC = "egds.greeting.events.test";

    @Mock
    private KafkaTemplate<String, GreetingEvent> kafkaTemplate;

    private GreetingEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new GreetingEventPublisher(kafkaTemplate, TOPIC);
    }

    @Test
    @DisplayName("publish delegates to KafkaTemplate.send with the correct topic and key")
    void publish_sendsToCorrectTopicWithCorrelationIdAsKey() {
        GreetingEvent event = new GreetingEvent("corr-001", "127.0.0.1", "greeting.admin");
        CompletableFuture<SendResult<String, GreetingEvent>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(eq(TOPIC), eq("corr-001"), eq(event))).thenReturn(future);

        publisher.publish(event);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<GreetingEvent> payloadCaptor = ArgumentCaptor.forClass(GreetingEvent.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo(TOPIC);
        assertThat(keyCaptor.getValue()).isEqualTo("corr-001");
        assertThat(payloadCaptor.getValue().getRequestIp()).isEqualTo("127.0.0.1");
    }

    @Test
    @DisplayName("publish returns a CompletableFuture from the KafkaTemplate")
    void publish_returnsCompletableFuture() {
        GreetingEvent event = new GreetingEvent("corr-002", "10.0.0.1", "greeting.admin");
        CompletableFuture<SendResult<String, GreetingEvent>> expectedFuture =
                new CompletableFuture<>();
        when(kafkaTemplate.send(eq(TOPIC), eq("corr-002"), eq(event)))
                .thenReturn(expectedFuture);

        CompletableFuture<SendResult<String, GreetingEvent>> result = publisher.publish(event);

        assertThat(result).isSameAs(expectedFuture);
    }
}
