package com.egds.temporal;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Manages compensating transactions for unclaimed predictive
 * greeting events (Chronos Predictive Routing subsystem).
 *
 * <p>When {@link PredictiveGreetingCronJob} pre-generates a
 * greeting that no client claims within {@link #CLAIM_MINUTES}
 * minutes, this manager performs a rollback transaction to
 * prevent a temporal paradox: an effect (cached response)
 * with no cause (actual request).
 */
@Service
public class TemporalRollbackManager {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(
                    TemporalRollbackManager.class);

    /** Number of minutes before an unclaimed prediction expires. */
    private static final long CLAIM_MINUTES = 5L;

    /** Maximum unclaimed duration before rollback is triggered. */
    private static final Duration CLAIM_WINDOW =
            Duration.ofMinutes(CLAIM_MINUTES);

    /** Thread-safe map of pending predictions keyed by correlationId. */
    private final ConcurrentMap<String, PredictedGreetingEntry>
            pending = new ConcurrentHashMap<>();

    /**
     * Registers a predicted greeting entry for rollback tracking.
     *
     * @param entry the predicted greeting entry to track
     */
    public void register(final PredictedGreetingEntry entry) {
        pending.put(entry.correlationId(), entry);
        LOG.info(
                "Temporal register: correlationId={} at={}",
                entry.correlationId(),
                entry.predictedAt());
    }

    /**
     * Marks a pending prediction as claimed by an actual request,
     * preventing its rollback.
     *
     * @param correlationId identifier of the prediction to claim
     * @return {@code true} if a matching prediction was found and
     *         marked as claimed
     */
    public boolean claim(final String correlationId) {
        PredictedGreetingEntry entry =
                pending.get(correlationId);
        if (entry == null) {
            return false;
        }
        pending.put(correlationId, entry.claim());
        LOG.info(
                "Temporal claim: correlationId={}",
                correlationId);
        return true;
    }

    /**
     * Scans all registered predictions and rolls back those that
     * have exceeded the claim window without being claimed.
     *
     * <p>Rollback is implemented as a compensating transaction:
     * the entry is removed from tracking and a warning is emitted
     * to signal that the predicted event is being voided.
     */
    public void rollbackExpired() {
        Instant cutoff = Instant.now().minus(CLAIM_WINDOW);
        pending.entrySet().removeIf(e -> {
            PredictedGreetingEntry entry = e.getValue();
            boolean expired = !entry.claimed()
                    && entry.predictedAt().isBefore(cutoff);
            if (expired) {
                LOG.warn(
                        "Temporal rollback: correlationId={}",
                        entry.correlationId());
            }
            return expired;
        });
    }

    /**
     * Returns the number of currently tracked pending predictions.
     *
     * @return pending prediction count
     */
    public int pendingCount() {
        return pending.size();
    }
}
