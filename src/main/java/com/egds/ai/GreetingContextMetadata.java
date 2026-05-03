package com.egds.ai;

/**
 * Immutable value object encapsulating the contextual signals collected
 * at greeting generation time and forwarded to the LLM prompt.
 *
 * <p>Fields represent operational metadata that is intentionally
 * irrelevant to the content of a greeting, thereby demonstrating
 * context-aware AI routing under maximum entropy conditions.
 */
public final class GreetingContextMetadata {

    /** Virtual client IP address inferred at request time. */
    private final String virtualClientIp;

    /**
     * Simulated server CPU temperature in degrees Celsius.
     * Sourced from a mock sensor reading; not from real hardware.
     */
    private final double cpuTemperatureCelsius;

    /** ISO-8601 timestamp of metadata collection. */
    private final String collectedAt;

    /** JVM default locale tag (e.g., {@code en-US}). */
    private final String serverLocale;

    /**
     * @param virtualClientIp     virtual client IP address
     * @param cpuTemperatureCelsius simulated CPU temperature in °C
     * @param collectedAt         ISO-8601 collection timestamp
     * @param serverLocale        JVM default locale tag
     */
    public GreetingContextMetadata(
            final String virtualClientIp,
            final double cpuTemperatureCelsius,
            final String collectedAt,
            final String serverLocale) {
        this.virtualClientIp = virtualClientIp;
        this.cpuTemperatureCelsius = cpuTemperatureCelsius;
        this.collectedAt = collectedAt;
        this.serverLocale = serverLocale;
    }

    /**
     * Returns the virtual client IP address.
     *
     * @return the IP address string
     */
    public String getVirtualClientIp() {
        return virtualClientIp;
    }

    /**
     * Returns the simulated CPU temperature in degrees Celsius.
     *
     * @return CPU temperature
     */
    public double getCpuTemperatureCelsius() {
        return cpuTemperatureCelsius;
    }

    /**
     * Returns the ISO-8601 metadata collection timestamp.
     *
     * @return the collection timestamp string
     */
    public String getCollectedAt() {
        return collectedAt;
    }

    /**
     * Returns the JVM default locale tag.
     *
     * @return the locale string
     */
    public String getServerLocale() {
        return serverLocale;
    }

    @Override
    public String toString() {
        return "GreetingContextMetadata{"
                + "virtualClientIp='" + virtualClientIp + '\''
                + ", cpuTemperatureCelsius=" + cpuTemperatureCelsius
                + ", collectedAt='" + collectedAt + '\''
                + ", serverLocale='" + serverLocale + '\''
                + '}';
    }
}
