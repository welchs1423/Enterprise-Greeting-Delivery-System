package com.egds.agile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Simulates a mandatory daily scrum standup ceremony by blocking
 * the calling thread for a configurable duration and emitting
 * standup prompts to the application log.
 *
 * <p>Injected into {@link com.egds.core.pipeline.MessageDeliveryPipeline}
 * to intercept each delivery execution cycle with a synchronous pause.
 * Set {@code egds.scrum.standup.duration-ms=0} to suppress the delay
 * in test environments.
 */
@Component
public class ScrumMasterDaemon {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(ScrumMasterDaemon.class);

    /** Duration of the mandatory standup pause in milliseconds. */
    private final long standupDurationMs;

    /**
     * @param durationMs standup pause duration in milliseconds;
     *                   controlled by {@code egds.scrum.standup.duration-ms}
     */
    public ScrumMasterDaemon(
            @Value("${egds.scrum.standup.duration-ms:2000}")
            final long durationMs) {
        this.standupDurationMs = durationMs;
    }

    /**
     * Conducts a synchronous daily standup ceremony.
     * Logs the three canonical standup prompts and blocks the calling
     * thread for {@link #standupDurationMs} milliseconds.
     * Restores the interrupt flag if the sleep is interrupted.
     */
    public void conductDailyStandup() {
        LOG.info("[SCRUM] Daily standup initiated by ScrumMasterDaemon.");
        LOG.info("[SCRUM] What did you do yesterday?");
        LOG.info("[SCRUM] What will you do today?");
        LOG.info("[SCRUM] Any blockers?");
        if (standupDurationMs > 0) {
            try {
                Thread.sleep(standupDurationMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.warn("[SCRUM] Standup interrupted.");
            }
        }
        LOG.info("[SCRUM] Standup complete. Resuming pipeline.");
    }
}
