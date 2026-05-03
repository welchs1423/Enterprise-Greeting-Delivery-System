package com.egds.core.strategy;

import com.egds.blockchain.BlockchainIntegrityException;
import com.egds.blockchain.GreetingIntegrityVerifier;
import com.egds.core.entity.MessageEntity;
import com.egds.core.enums.DeliveryStatus;
import com.egds.core.exception.MessageDeliveryFailureException;
import com.egds.core.interfaces.IMessageOutputStrategy;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * {@link IMessageOutputStrategy} implementation targeting the standard
 * output stream. Delivers the formatted content of a
 * {@link MessageEntity} to {@code System.out}.
 *
 * <p>Before writing to stdout, the formatted content of the entity is
 * verified against the Keccak-256 fingerprint stored in the mock
 * Ethereum smart contract via {@link GreetingIntegrityVerifier#verify}.
 * If the stored hash does not match the recomputed hash, a
 * {@link BlockchainIntegrityException} is thrown and the fallback emits
 * an integrity-violation marker instead of the tampered payload.
 *
 * <p>Each invocation is instrumented with an OpenTelemetry
 * {@link Span} to enable distributed trace correlation. The write path
 * is guarded by a Resilience4j Circuit Breaker (to prevent cascading
 * failures), Rate Limiter (to cap burst output throughput), and Retry
 * (to recover from transient write errors before tripping the breaker).
 *
 * <p>When the circuit breaker is open or all retry attempts are
 * exhausted, the fallback emits a degraded-mode greeting to stdout
 * and records the delivery as {@link DeliveryStatus#FAILED}.
 */
@Component
public class ConsoleOutputStrategy implements IMessageOutputStrategy {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(ConsoleOutputStrategy.class);

    /** Greeting substituted when the circuit breaker is open. */
    private static final String FALLBACK_MESSAGE =
            "[EGDS-DEGRADED] Hello, World!"
            + " (circuit open - output subsystem degraded)";

    /** Marker emitted when blockchain integrity verification fails. */
    private static final String INTEGRITY_VIOLATION_MESSAGE =
            "[EGDS-INTEGRITY-VIOLATION] delivery blocked:"
            + " payload hash mismatch detected by smart contract";

    /** Resilience4j instance name for all three patterns. */
    private static final String RESILIENCE_NAME = "consoleOutput";

    /** OTel span name for console delivery operations. */
    private static final String SPAN_NAME = "egds.console-output";

    /** Tracer used to create and manage delivery spans. */
    private final Tracer tracer;

    /** Blockchain verifier for pre-output content integrity checks. */
    private final GreetingIntegrityVerifier integrityVerifier;

    /**
     * @param tracer   the Micrometer Tracing tracer for span creation
     * @param verifier the blockchain integrity verifier
     */
    public ConsoleOutputStrategy(
            final Tracer tracer,
            final GreetingIntegrityVerifier verifier) {
        this.tracer = tracer;
        this.integrityVerifier = verifier;
    }

    /**
     * Writes the formatted content of the supplied entity to stdout.
     * Transitions the entity's delivery status to IN_TRANSIT prior to
     * write and to DELIVERED on success, or FAILED on error.
     *
     * <p>An OTel span is created for each invocation.  The span carries
     * {@code egds.correlationId} and {@code egds.deliveryStatus} tags.
     * On failure the span is marked with the thrown exception.
     *
     * <p>The method is decorated with, in application order:
     * <ol>
     *   <li>{@code @CircuitBreaker} — open after 50% failure rate
     *       over 10 calls; fallback is {@link #outputFallback}.</li>
     *   <li>{@code @RateLimiter} — caps at 50 calls/s with a 500 ms
     *       acquisition timeout; fallback is {@link #outputFallback}.</li>
     *   <li>{@code @Retry} — up to 3 attempts with 200 ms fixed
     *       wait between tries.</li>
     * </ol>
     *
     * @param messageEntity the finalized entity to deliver; must not be
     *                      null
     * @throws MessageDeliveryFailureException if entity is null or the
     *         output stream cannot be written to
     * @throws BlockchainIntegrityException if the entity's formatted
     *         content does not match the fingerprint in the mock contract
     */
    @CircuitBreaker(name = RESILIENCE_NAME, fallbackMethod = "outputFallback")
    @RateLimiter(name = RESILIENCE_NAME, fallbackMethod = "outputFallback")
    @Retry(name = RESILIENCE_NAME)
    @Override
    public void output(final MessageEntity messageEntity) {
        if (messageEntity == null) {
            throw new MessageDeliveryFailureException(
                    "ConsoleOutputStrategy received a null MessageEntity.",
                    "UNKNOWN",
                    "ERR_NULL_ENTITY"
            );
        }
        Span span = tracer.nextSpan()
                .name(SPAN_NAME)
                .tag("egds.correlationId", messageEntity.getCorrelationId())
                .tag("egds.layer", "output-strategy")
                .start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            messageEntity.setDeliveryStatus(DeliveryStatus.IN_TRANSIT);

            integrityVerifier.verify(
                    messageEntity.getCorrelationId(),
                    messageEntity.getFormattedContent());
            span.tag("egds.blockchain.integrity", "VERIFIED");

            LOG.info("[OUTPUT] Writing to stdout"
                    + " correlationId={} traceId={}",
                    messageEntity.getCorrelationId(),
                    span.context().traceId());
            System.out.println(messageEntity.getFormattedContent());
            messageEntity.setDeliveryStatus(DeliveryStatus.DELIVERED);
            span.tag("egds.deliveryStatus", "DELIVERED");
        } catch (BlockchainIntegrityException e) {
            messageEntity.setDeliveryStatus(DeliveryStatus.FAILED);
            span.tag("egds.deliveryStatus", "FAILED");
            span.tag("egds.blockchain.integrity", "VIOLATED");
            span.error(e);
            LOG.error("[BLOCKCHAIN] integrity violation correlationId={}",
                    messageEntity.getCorrelationId());
            System.out.println(INTEGRITY_VIOLATION_MESSAGE);
            throw e;
        } catch (Exception e) {
            messageEntity.setDeliveryStatus(DeliveryStatus.FAILED);
            span.tag("egds.deliveryStatus", "FAILED");
            span.error(e);
            throw new MessageDeliveryFailureException(
                    "ConsoleOutputStrategy failed during output write.",
                    messageEntity.getCorrelationId(),
                    "ERR_OUTPUT_WRITE_FAILURE",
                    e
            );
        } finally {
            span.end();
        }
    }

    /**
     * Fallback invoked when the circuit breaker is open, the rate limit
     * is exceeded, or all retry attempts are exhausted.
     *
     * <p>Emits a degraded-mode greeting string to stdout so that the
     * caller always receives some output. The entity's delivery status is
     * set to {@link DeliveryStatus#FAILED} to record the degradation in
     * the audit log.
     *
     * @param messageEntity the entity that could not be delivered
     * @param t             the cause that triggered the fallback
     */
    void outputFallback(
            final MessageEntity messageEntity, final Throwable t) {
        final String correlationId = (messageEntity != null)
                ? messageEntity.getCorrelationId() : "UNKNOWN";
        LOG.warn("[CB-FALLBACK] consoleOutput circuit activated."
                + " correlationId={} cause={}",
                correlationId, t.getMessage());
        System.out.println(FALLBACK_MESSAGE);
        if (messageEntity != null) {
            messageEntity.setDeliveryStatus(DeliveryStatus.FAILED);
        }
    }
}
