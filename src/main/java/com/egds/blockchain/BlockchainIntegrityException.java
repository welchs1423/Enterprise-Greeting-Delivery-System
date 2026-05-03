package com.egds.blockchain;

/**
 * Thrown when the Keccak-256 hash of an outbound greeting payload does
 * not match the fingerprint stored in the mock Ethereum smart contract
 * registry, or when the registry contains no record for the given
 * correlation identifier.
 *
 * <p>This exception surfaces inside
 * {@link com.egds.core.strategy.ConsoleOutputStrategy} and triggers the
 * Resilience4j fallback, preventing tampered content from reaching the
 * output channel.
 */
public final class BlockchainIntegrityException extends RuntimeException {

    /** Correlation ID of the delivery whose integrity check failed. */
    private final String correlationId;

    /**
     * Constructs a new {@code BlockchainIntegrityException}.
     *
     * @param message       description of the integrity violation
     * @param correlationId correlation ID of the affected delivery cycle
     */
    public BlockchainIntegrityException(
            final String message,
            final String correlationId) {
        super(message);
        this.correlationId = correlationId;
    }

    /**
     * Returns the correlation ID of the delivery that failed integrity
     * verification.
     *
     * @return the correlation ID string
     */
    public String getCorrelationId() {
        return correlationId;
    }
}
