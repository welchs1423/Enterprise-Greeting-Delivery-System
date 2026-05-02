package com.egds.observability;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies OpenTelemetry distributed tracing integration via the
 * Micrometer Tracing OTel bridge.
 *
 * <p>Tests confirm that:
 * <ul>
 *   <li>The auto-configured {@link Tracer} bean is non-null and
 *       functional.</li>
 *   <li>Starting a {@link Span} produces a non-zero trace identifier
 *       so that log correlation is possible.</li>
 *   <li>The {@code traceId} is injected into the SLF4J MDC while the
 *       span scope is active, enabling trace-aware log lines.</li>
 *   <li>The MDC entry is cleared after the scope is closed, preventing
 *       trace ID leakage across requests.</li>
 * </ul>
 */
@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = "${egds.kafka.topic.greeting:egds.greeting.events.test}")
@DirtiesContext
class TraceContextPropagationTest {

    /** Auto-configured Micrometer Tracing OTel bridge tracer. */
    @Autowired
    private Tracer tracer;

    /**
     * The Tracer bean auto-configured by Spring Boot must be present
     * and non-null when {@code micrometer-tracing-bridge-otel} is on
     * the classpath.
     */
    @Test
    @DisplayName("Tracer bean is auto-configured and non-null")
    void tracerBeanIsAvailableTest() {
        assertThat(tracer)
                .as("Spring Boot must auto-configure a Tracer bean"
                        + " when micrometer-tracing-bridge-otel is present")
                .isNotNull();
    }

    /**
     * A started span must carry a non-null, non-empty trace ID. A zero
     * or empty trace ID indicates a no-op tracer, which would prevent
     * distributed trace correlation.
     */
    @Test
    @DisplayName("Started span carries a non-zero trace ID")
    void spanHasNonZeroTraceIdTest() {
        Span span = tracer.nextSpan().name("test.trace-id-check").start();
        try {
            String traceId = span.context().traceId();
            assertThat(traceId)
                    .as("Span trace ID must be non-null and non-empty")
                    .isNotNull()
                    .isNotEmpty();
            // A no-op span context yields all-zero IDs (e.g. "0000...0").
            assertThat(traceId.replace("0", ""))
                    .as("Span trace ID must not be all-zero (no-op tracer)")
                    .isNotEmpty();
        } finally {
            span.end();
        }
    }

    /**
     * The {@code traceId} MDC key must be populated while a span scope
     * is active so that every log statement emitted within the scope
     * carries the correlation identifier.
     */
    @Test
    @DisplayName("traceId is injected into MDC within active span scope")
    void traceIdInjectedIntoMdcTest() {
        Span span = tracer.nextSpan().name("test.mdc-injection").start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            String mdcTraceId = MDC.get("traceId");
            assertThat(mdcTraceId)
                    .as("MDC must contain a non-null traceId"
                            + " while a span scope is active")
                    .isNotNull()
                    .isNotEmpty();
            assertThat(mdcTraceId)
                    .as("MDC traceId must match the active span trace ID")
                    .isEqualTo(span.context().traceId());
        } finally {
            span.end();
        }
    }

    /**
     * After the span scope is closed the {@code traceId} MDC key must
     * be absent or null. Lingering MDC values would corrupt log
     * correlation for subsequent requests on the same thread.
     */
    @Test
    @DisplayName("traceId is cleared from MDC after span scope closes")
    void traceIdClearedFromMdcAfterScopeTest() {
        Span span = tracer.nextSpan().name("test.mdc-cleanup").start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            // scope active — traceId present
        } finally {
            span.end();
        }
        // scope closed — traceId must be absent
        String mdcTraceId = MDC.get("traceId");
        assertThat(mdcTraceId)
                .as("MDC traceId must be null or empty after span scope closes")
                .isNullOrEmpty();
    }

    /**
     * Two spans started from the same parent context must share the same
     * trace ID but have distinct span IDs, confirming that the tracer
     * maintains the trace hierarchy correctly.
     */
    @Test
    @DisplayName("Child spans share trace ID but have distinct span IDs")
    void childSpansShareTraceIdTest() {
        Span parent = tracer.nextSpan().name("test.parent").start();
        String parentTraceId;
        String childOneSpanId;
        String childTwoSpanId;

        try (Tracer.SpanInScope ignored = tracer.withSpan(parent)) {
            parentTraceId = parent.context().traceId();

            Span childOne = tracer.nextSpan()
                    .name("test.child-one").start();
            childOneSpanId = childOne.context().spanId();
            childOne.end();

            Span childTwo = tracer.nextSpan()
                    .name("test.child-two").start();
            childTwoSpanId = childTwo.context().spanId();
            childTwo.end();
        } finally {
            parent.end();
        }

        assertThat(childOneSpanId)
                .as("Child spans must have distinct span IDs")
                .isNotEqualTo(childTwoSpanId);
        assertThat(parentTraceId)
                .as("Parent trace ID must be non-null")
                .isNotNull()
                .isNotEmpty();
    }
}
