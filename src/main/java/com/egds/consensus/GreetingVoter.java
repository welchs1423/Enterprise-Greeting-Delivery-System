package com.egds.consensus;

/**
 * Functional contract for a single participant in the
 * {@link ConsensusVotingEngine} microservice parliament.
 *
 * <p>Each implementation represents an autonomous module that
 * independently decides whether a greeting request is worthy
 * of delivery. All voters must approve for delivery to proceed.
 */
public interface GreetingVoter {

    /**
     * Returns this voter's display name used in audit logs.
     *
     * @return non-null voter identifier
     */
    String name();

    /**
     * Casts a vote on whether the greeting should be delivered.
     *
     * @param correlationId request correlation identifier
     * @return {@code true} to approve, {@code false} to deny
     */
    boolean vote(String correlationId);
}
