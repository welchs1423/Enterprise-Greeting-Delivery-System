package com.egds.blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Performs a SHA-256 Proof-of-Work calculation before a greeting is
 * delivered.  Iterates nonce values until the digest of the candidate
 * string begins with the required difficulty prefix, simulating a
 * virtual gas-fee payment.
 */
@Component
public class GreetingCoinMiner {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(GreetingCoinMiner.class);

    /** Hex prefix that a valid SHA-256 digest must begin with. */
    static final String DIFFICULTY_PREFIX = "00";

    /** Bitmask to treat a signed byte as unsigned for hex formatting. */
    private static final int UNSIGNED_BYTE_MASK = 0xff;

    /**
     * Mines a valid nonce by hashing {@code correlationId + nonce} until
     * the result starts with {@value #DIFFICULTY_PREFIX}.
     *
     * @param correlationId delivery cycle identifier used as the mining seed
     * @return the {@link MiningResult} containing the winning nonce and hash
     */
    public MiningResult mine(final String correlationId) {
        LOG.info("[MINER] PoW started correlationId={}", correlationId);
        long nonce = 0L;
        while (true) {
            String candidate = correlationId + nonce;
            String hash = sha256(candidate);
            if (hash.startsWith(DIFFICULTY_PREFIX)) {
                LOG.info(
                        "[MINER] Valid nonce={} hash={} correlationId={}",
                        nonce, hash, correlationId);
                return new MiningResult(nonce, hash);
            }
            nonce++;
        }
    }

    /**
     * Computes the SHA-256 hex digest of the given input string.
     *
     * @param input the string to hash
     * @return lowercase hex digest string
     */
    private String sha256(final String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(
                    input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b & UNSIGNED_BYTE_MASK));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    /**
     * Immutable record holding the result of a successful PoW round.
     *
     * @param nonce the nonce value that produced a valid hash
     * @param hash  SHA-256 hex string beginning with the difficulty prefix
     */
    public record MiningResult(long nonce, String hash) { }
}
