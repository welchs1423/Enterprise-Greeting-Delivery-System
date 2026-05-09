package com.egds.multiverse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;

/**
 * Validates multiverse consistency by comparing greeting hashes across
 * universe branches.
 *
 * <p>For each greeting the manager computes a SHA-256 hash in the primary
 * JVM context, then re-computes the same hash inside a sandboxed parallel
 * universe context using an isolated {@link MultiverseClassLoader}. If the
 * two hashes match, the multiverse is consistent. If they diverge, a
 * {@link DimensionalRiftException} is raised.
 *
 * <p>The isolated classloader ensures that the parallel-universe computation
 * runs in a separate class namespace, preventing shared static state from
 * masking divergence.
 */
@Service
public class MultiverseConsistencyManager {

    private static final Logger LOG =
            LoggerFactory.getLogger(MultiverseConsistencyManager.class);

    private static final String HASH_SERVICE_CLASS =
            "com.egds.multiverse.GreetingHashService";

    private static final String COMPUTE_HASH_METHOD = "computeHash";

    /**
     * Verifies that the greeting hash is consistent across universe branches.
     *
     * <p>Loads {@link GreetingHashService} in an isolated classloader to
     * simulate parallel-universe execution and compares the resulting hash
     * against the primary-universe result.
     *
     * @param correlationId request identifier for audit tracing
     * @param greetingText  the greeting payload to verify
     * @throws DimensionalRiftException if the parallel-universe hash differs
     *                                  from the primary-universe hash
     */
    public void verifyConsistency(
            String correlationId, String greetingText) {
        String primaryHash = GreetingHashService.computeHash(greetingText);
        String parallelHash = computeInParallelUniverse(greetingText);
        LOG.info("[MULTIVERSE] consistency check correlationId={}"
                + " primary_hash={} parallel_hash={}",
                correlationId, primaryHash, parallelHash);
        if (!primaryHash.equals(parallelHash)) {
            throw new DimensionalRiftException(
                    "Dimensional rift detected:"
                    + " correlationId=" + correlationId
                    + " primary=" + primaryHash
                    + " parallel=" + parallelHash);
        }
        LOG.info("[MULTIVERSE] consistency verified"
                + " correlationId={}", correlationId);
    }

    /**
     * Loads {@link GreetingHashService} in an isolated classloader and
     * invokes its hash computation reflectively.
     *
     * @param greetingText input to hash in the parallel-universe context
     * @return SHA-256 hex digest produced by the isolated class instance
     * @throws DimensionalRiftException if classloading or reflection fails
     */
    private String computeInParallelUniverse(String greetingText) {
        try {
            MultiverseClassLoader loader = new MultiverseClassLoader();
            Class<?> clazz = loader.loadClass(HASH_SERVICE_CLASS);
            return (String) clazz
                    .getMethod(COMPUTE_HASH_METHOD, String.class)
                    .invoke(null, greetingText);
        } catch (ClassNotFoundException
                | NoSuchMethodException
                | IllegalAccessException
                | InvocationTargetException e) {
            throw new DimensionalRiftException(
                    "Parallel-universe sandbox failed: " + e.getMessage());
        }
    }
}
