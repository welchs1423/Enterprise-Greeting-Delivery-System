/**
 * Observability infrastructure for the EGDS platform (Phase 4).
 *
 * <p>This package provides OpenTelemetry distributed tracing integration
 * via the Micrometer Tracing OTel bridge. Spans created by classes in
 * this layer and in the instrumented service classes ({@link
 * com.egds.core.strategy.ConsoleOutputStrategy}, {@link
 * com.egds.messaging.GreetingEventPublisher}, {@link
 * com.egds.messaging.GreetingEventConsumer}, {@link
 * com.egds.core.service.MessageDeliveryService}) propagate a common
 * trace identifier through every stage of a single "Hello, World!"
 * delivery cycle.
 *
 * <p>The {@code traceId} and {@code spanId} values are injected into
 * the MDC by Micrometer Tracing and are emitted in every log line via
 * the configured logging pattern.
 *
 * <p>Prometheus metrics are exposed at
 * {@code /actuator/prometheus} and include Resilience4j circuit breaker
 * state, rate limiter usage, retry statistics, and standard JVM, HTTP,
 * and Kafka producer/consumer metrics.
 */
package com.egds.observability;
