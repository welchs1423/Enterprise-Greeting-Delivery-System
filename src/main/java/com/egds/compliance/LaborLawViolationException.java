package com.egds.compliance;

/**
 * Thrown when the system's cumulative uptime exceeds the configured
 * statutory working-hours threshold. All Resilience4j circuit breakers
 * are forced open immediately before this exception is raised.
 */
public class LaborLawViolationException extends RuntimeException {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Measured cumulative uptime in hours that triggered this violation. */
    private final long cumulativeUptimeHours;

    /** Configured threshold in hours that was exceeded. */
    private final long thresholdHours;

    /**
     * Constructs a new violation exception.
     *
     * @param uptimeHours total uptime hours accumulated
     * @param limitHours  legal maximum uptime threshold
     */
    public LaborLawViolationException(
            final long uptimeHours,
            final long limitHours) {
        super("Cumulative uptime " + uptimeHours
                + "h exceeds " + limitHours + "h limit");
        this.cumulativeUptimeHours = uptimeHours;
        this.thresholdHours = limitHours;
    }

    /**
     * Returns the cumulative uptime in hours that triggered this
     * violation.
     *
     * @return cumulative uptime hours
     */
    public long getCumulativeUptimeHours() {
        return cumulativeUptimeHours;
    }

    /**
     * Returns the legal maximum uptime threshold in hours.
     *
     * @return threshold hours
     */
    public long getThresholdHours() {
        return thresholdHours;
    }
}
