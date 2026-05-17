package com.egds.msa;

import java.util.Map;

/**
 * Bloated HTTP response wrapper mandated by the V22 enterprise
 * compliance framework.
 *
 * <p>Embeds the original {@code correlationId}, {@code status},
 * and {@code message} fields at the top level alongside a
 * {@code metadata} node carrying carbon-emission estimates,
 * GDPR disclaimers, fake environment diagnostics, and
 * other compliance artefacts that materially increase payload
 * size without adding business value.
 */
public final class BloatedCompliancePayload {

    /** Unique correlation ID from the original greeting response. */
    private final String correlationId;

    /** Acceptance status string from the original response. */
    private final String status;

    /** Human-readable delivery message from the original response. */
    private final String message;

    /** Compliance metadata map with carbon, GDPR, and env fields. */
    private final Map<String, String> metadata;

    /**
     * @param corrId       the delivery correlation identifier
     * @param statusVal    the acceptance status string
     * @param messageVal   the human-readable acceptance message
     * @param metadataMap  immutable compliance metadata map
     */
    public BloatedCompliancePayload(
            final String corrId,
            final String statusVal,
            final String messageVal,
            final Map<String, String> metadataMap) {
        this.correlationId = corrId;
        this.status = statusVal;
        this.message = messageVal;
        this.metadata = Map.copyOf(metadataMap);
    }

    /**
     * Returns the correlation ID.
     *
     * @return the correlation ID string
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Returns the acceptance status.
     *
     * @return the status string
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns the human-readable acceptance message.
     *
     * @return the message string
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the compliance metadata map.
     *
     * @return an unmodifiable map of compliance key-value pairs
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }
}
