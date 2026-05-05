package com.egds.temporal;

import java.time.Instant;

/**
 * Immutable record representing a pre-generated greeting cached
 * by the Chronos Predictive Routing subsystem.
 *
 * <p>Each entry tracks whether a client has claimed it, enabling
 * {@link TemporalRollbackManager} to issue compensating
 * transactions for unclaimed predictions.
 *
 * @param correlationId unique prediction identifier
 * @param content       pre-generated greeting payload
 * @param predictedAt   wall-clock instant of prediction
 * @param claimed       whether a real request has claimed this entry
 */
public record PredictedGreetingEntry(
        String correlationId,
        String content,
        Instant predictedAt,
        boolean claimed) {

    /**
     * Returns a copy of this entry with {@code claimed} set to
     * {@code true}.
     *
     * @return claimed variant of this entry
     */
    public PredictedGreetingEntry claim() {
        return new PredictedGreetingEntry(
                correlationId, content, predictedAt, true);
    }
}
