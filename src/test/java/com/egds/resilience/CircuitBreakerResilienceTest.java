package com.egds.resilience;

import com.egds.core.entity.MessageEntity;
import com.egds.core.enums.DeliveryStatus;
import com.egds.core.enums.MessagePriority;
import com.egds.core.exception.MessageDeliveryFailureException;
import com.egds.core.strategy.ConsoleOutputStrategy;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies Resilience4j Circuit Breaker behavior on the
 * {@link ConsoleOutputStrategy} delivery path.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>Initial state: circuit breaker starts CLOSED.</li>
 *   <li>Transition to OPEN after the configured failure rate threshold
 *       (50% over a 10-call sliding window) is exceeded.</li>
 *   <li>Fallback output: degraded-mode greeting emitted when OPEN.</li>
 *   <li>Successful delivery: circuit remains CLOSED on success.</li>
 * </ul>
 */
@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = "${egds.kafka.topic.greeting:egds.greeting.events.test}")
@DirtiesContext
class CircuitBreakerResilienceTest {

    /** Name of the Circuit Breaker instance under test. */
    private static final String CB_NAME = "consoleOutput";

    /** Number of calls that compose the CB sliding window. */
    private static final int SLIDING_WINDOW = 10;

    /**
     * Failure calls required to open the CB at 50% threshold over 10
     * calls. Must equal or exceed ceil(window * threshold).
     */
    private static final int FAILURES_TO_OPEN = 6;

    /** R4j registry for inspecting CB state. */
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    /** Strategy under test — Spring proxy with R4j decorators applied. */
    @Autowired
    private ConsoleOutputStrategy consoleOutputStrategy;

    /**
     * Resets the circuit breaker to CLOSED before each test so that
     * state accumulated by earlier tests does not affect subsequent ones.
     */
    @BeforeEach
    void resetCircuitBreaker() {
        circuitBreakerRegistry.circuitBreaker(CB_NAME).reset();
    }

    /**
     * A new circuit breaker must start in the CLOSED state before any
     * calls have been recorded.
     */
    @Test
    @DisplayName("Circuit breaker starts CLOSED")
    void circuitBreakerInitiallyClosedTest() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(CB_NAME);
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    /**
     * After enough failures to cross the 50% threshold within the
     * 10-call sliding window, the circuit breaker must transition to
     * the OPEN state.
     */
    @Test
    @DisplayName("Circuit breaker opens after failure rate threshold")
    void circuitBreakerOpensAfterFailuresTest() {
        // Force the CB to record failures by passing a null entity,
        // which triggers MessageDeliveryFailureException. The CB Retry
        // is also active, so each logical call retries up to 3 times
        // internally; the CB sees one final failure per logical call.
        for (int i = 0; i < FAILURES_TO_OPEN; i++) {
            try {
                consoleOutputStrategy.output(buildNullCausingEntity());
            } catch (Exception ignored) {
                // expected — keep looping to accumulate CB failures
            }
        }
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(CB_NAME);
        assertThat(cb.getState())
                .as("CB must be OPEN after %d failures over a %d-call window",
                        FAILURES_TO_OPEN, SLIDING_WINDOW)
                .isEqualTo(CircuitBreaker.State.OPEN);
    }

    /**
     * When the circuit breaker is OPEN, the output method must invoke
     * the fallback and write the degraded-mode message to stdout.
     */
    @Test
    @DisplayName("Fallback message written to stdout when circuit is OPEN")
    void fallbackOutputWhenCircuitOpenTest() {
        // Open the circuit.
        for (int i = 0; i < FAILURES_TO_OPEN; i++) {
            try {
                consoleOutputStrategy.output(buildNullCausingEntity());
            } catch (Exception ignored) {
                // accumulate failures
            }
        }

        // Capture stdout for the fallback call.
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(captured));
        try {
            MessageEntity entity = buildValidEntity("fallback-trace-001");
            consoleOutputStrategy.output(entity);
        } finally {
            System.setOut(original);
        }

        assertThat(captured.toString())
                .as("Fallback must write the degraded-mode greeting")
                .contains("EGDS-DEGRADED");
    }

    /**
     * A single successful delivery on a CLOSED circuit must not cause
     * any state transition; the circuit remains CLOSED.
     */
    @Test
    @DisplayName("Successful delivery keeps circuit CLOSED")
    void successfulDeliveryKeepsCircuitClosedTest() {
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(captured));
        try {
            consoleOutputStrategy.output(buildValidEntity("success-001"));
        } finally {
            System.setOut(original);
        }

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(CB_NAME);
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        assertThat(captured.toString()).isNotBlank();
    }

    /**
     * Builds a {@link MessageEntity} whose {@code getFormattedContent()}
     * call will throw, causing the CB to record a failure.
     *
     * @return a MessageEntity that will trigger a failure
     */
    private MessageEntity buildNullCausingEntity() {
        // A null entity triggers ERR_NULL_ENTITY inside the strategy.
        return null;
    }

    /**
     * Builds a valid {@link MessageEntity} with the supplied correlation
     * ID for successful delivery scenarios.
     *
     * @param correlationId the correlation identifier for the entity
     * @return a fully populated, deliverable entity
     */
    private MessageEntity buildValidEntity(final String correlationId) {
        MessageEntity entity = new MessageEntity();
        entity.setCorrelationId(correlationId);
        entity.setContent("Hello, World!");
        entity.setPriority(MessagePriority.NORMAL);
        entity.setLocale("en-US");
        entity.setDeliveryStatus(DeliveryStatus.PENDING);
        return entity;
    }
}
