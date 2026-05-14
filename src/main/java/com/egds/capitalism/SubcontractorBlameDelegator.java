package com.egds.capitalism;

import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@link SubcontractorWorldProvider} implementation that delegates
 * World-component generation to a virtual subcontractor.
 *
 * <p>Fails with 30% probability when enabled to simulate subcontractor
 * unreliability. On failure, a {@link SubcontractorFailureException} is
 * thrown. The corporate exception handler then issues a liability
 * disclaimer on behalf of headquarters.
 *
 * <p>Set {@code egds.subcontractor.enabled=false} in test properties
 * to prevent probabilistic failures in automated tests.
 */
@Component
public class SubcontractorBlameDelegator
        implements SubcontractorWorldProvider {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(SubcontractorBlameDelegator.class);

    /** World component string returned on successful delivery. */
    private static final String WORLD_COMPONENT = "World";

    /** Failure probability numerator out of 100. */
    private static final int FAILURE_THRESHOLD = 30;

    /** Denominator for percentage-based failure probability. */
    private static final int PERCENT = 100;

    /**
     * When false, the failure condition never fires (e.g., in tests).
     */
    @Value("${egds.subcontractor.enabled:true}")
    private boolean subcontractorEnabled;

    /**
     * Produces the World component, failing with 30% probability
     * when {@code egds.subcontractor.enabled} is {@code true}.
     *
     * @return the World string on successful delivery
     * @throws SubcontractorFailureException with 30% probability
     *         when the subcontractor is enabled
     */
    @Override
    public String provideWorldComponent() {
        if (shouldFail()) {
            LOG.warn(
                    "[SUBCONTRACTOR] World component delivery failed. "
                    + "Escalating to corporate exception handler.");
            throw new SubcontractorFailureException(
                    "Subcontractor failed to deliver World component");
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(
                    "[SUBCONTRACTOR] World component delivered: {}",
                    WORLD_COMPONENT);
        }
        return WORLD_COMPONENT;
    }

    /**
     * Returns true with 30% probability when the subcontractor is
     * enabled. Returns false unconditionally otherwise.
     *
     * @return true if the failure condition fires on this evaluation
     */
    boolean shouldFail() {
        return subcontractorEnabled
                && ThreadLocalRandom.current()
                        .nextInt(PERCENT) < FAILURE_THRESHOLD;
    }
}
