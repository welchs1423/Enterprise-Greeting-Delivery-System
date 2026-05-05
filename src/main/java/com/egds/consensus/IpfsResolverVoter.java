package com.egds.consensus;

import org.springframework.stereotype.Component;

/**
 * IPFS Resolver participant in the microservice parliament.
 *
 * <p>Simulates a content-addressable lookup against an IPFS
 * node to verify that the requesting client has a valid
 * distributed profile pinned on the network. In production
 * this would call the IPFS HTTP API; the current implementation
 * applies a CIDv1 prefix heuristic: the first hex character of
 * the correlation ID must be in the range [0-9a-f].
 */
@Component
public class IpfsResolverVoter implements GreetingVoter {

    @Override
    public String name() {
        return "IPFS-Resolver";
    }

    /**
     * Approves delivery when the correlation ID starts with a
     * character in the range [0-9a-f], simulating a valid
     * CIDv1 content address prefix check against the IPFS node.
     *
     * @param correlationId request correlation identifier
     * @return {@code true} when the IPFS profile resolves
     */
    @Override
    public boolean vote(final String correlationId) {
        if (correlationId == null || correlationId.isEmpty()) {
            return false;
        }
        char first = correlationId.charAt(0);
        return (first >= '0' && first <= '9')
                || (first >= 'a' && first <= 'f');
    }
}
