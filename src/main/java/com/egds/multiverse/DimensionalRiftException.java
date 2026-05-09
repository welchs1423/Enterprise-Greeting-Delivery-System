package com.egds.multiverse;

/**
 * Thrown when the greeting hash computed in the parallel-universe JVM
 * context does not match the hash computed in the primary-universe context.
 *
 * <p>A rift indicates that the two universe branches have diverged in their
 * deterministic computation of greeting content, violating the multiverse
 * consistency invariant maintained by {@link MultiverseConsistencyManager}.
 */
public class DimensionalRiftException extends RuntimeException {

    /**
     * Constructs the exception with a descriptive message.
     *
     * @param message description of the detected divergence
     */
    public DimensionalRiftException(String message) {
        super(message);
    }
}
