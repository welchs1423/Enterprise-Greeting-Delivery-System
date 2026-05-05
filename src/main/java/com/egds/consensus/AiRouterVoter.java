package com.egds.consensus;

import org.springframework.stereotype.Component;

/**
 * AI Router participant in the microservice parliament.
 *
 * <p>Simulates a machine-learning inference call that evaluates
 * request metadata to determine greeting worthiness. In
 * production this would invoke the LangChain4j inference
 * pipeline; the current implementation applies a deterministic
 * hash of the correlation ID to approximate a 50% base rate.
 */
@Component
public final class AiRouterVoter implements GreetingVoter {

    @Override
    public String name() {
        return "AI-Router";
    }

    /**
     * Approves delivery when the correlation ID hash is even,
     * modelling the simulated 50% ML approval rate.
     *
     * @param correlationId request correlation identifier
     * @return {@code true} when the simulated model approves
     */
    @Override
    public boolean vote(final String correlationId) {
        return Math.abs(correlationId.hashCode()) % 2 == 0;
    }
}
