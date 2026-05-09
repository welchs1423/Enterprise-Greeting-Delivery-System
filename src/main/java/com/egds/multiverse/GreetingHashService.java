package com.egds.multiverse;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Computes a SHA-256 hex digest of a greeting string.
 *
 * <p>This class is intentionally free of external dependencies so that
 * it can be loaded and executed inside an isolated
 * {@link MultiverseClassLoader} without requiring cross-loader class
 * sharing beyond the JDK bootstrap layer.
 */
public final class GreetingHashService {

    private GreetingHashService() {
    }

    /**
     * Returns the SHA-256 hex digest of {@code greeting}.
     *
     * @param greeting input text; must not be null
     * @return lowercase hex string of the SHA-256 digest
     * @throws IllegalStateException if the SHA-256 algorithm is unavailable
     */
    public static String computeHash(final String greeting) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    greeting.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
