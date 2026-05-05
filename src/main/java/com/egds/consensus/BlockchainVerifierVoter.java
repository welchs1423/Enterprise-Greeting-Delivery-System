package com.egds.consensus;

import org.springframework.stereotype.Component;

/**
 * Blockchain Verifier participant in the microservice parliament.
 *
 * <p>Simulates an on-chain reputation lookup via the Web3j
 * Ethereum client. In production this would query a smart
 * contract for the caller's trust score; the current
 * implementation validates that the correlation ID conforms
 * to the standard UUID format, approximating a well-formed
 * on-chain identity token check.
 */
@Component
public final class BlockchainVerifierVoter implements GreetingVoter {

    /** Expected length of a standard UUID string representation. */
    private static final int UUID_LENGTH = 36;

    @Override
    public String name() {
        return "Blockchain-Verifier";
    }

    /**
     * Approves delivery when the correlation ID is a well-formed
     * UUID ({@value #UUID_LENGTH} characters), indicating a valid
     * on-chain identity token format.
     *
     * @param correlationId request correlation identifier
     * @return {@code true} when the chain verification passes
     */
    @Override
    public boolean vote(final String correlationId) {
        return correlationId != null
                && correlationId.length() == UUID_LENGTH;
    }
}
