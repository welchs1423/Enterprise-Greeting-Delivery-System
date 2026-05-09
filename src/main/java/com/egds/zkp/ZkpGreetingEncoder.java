package com.egds.zkp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Mock zero-knowledge proof encoder for EGDS greeting payloads.
 *
 * <p>Simulates a commitment scheme where the prover demonstrates
 * knowledge of the greeting without transmitting the plaintext.
 * The commitment is {@code SHA-256(message || nonce)} and the
 * verification key is {@code SHA-256(commitment || EGDS_VK_SALT)}.
 *
 * <p>A fresh 32-byte nonce is generated per call, ensuring that two
 * proofs of the same message are computationally unlinkable.
 *
 * <p>In a production system this would be replaced by a real
 * zk-SNARK circuit operating over a BLS12-381 elliptic curve field.
 */
@Component
public class ZkpGreetingEncoder {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(ZkpGreetingEncoder.class);

    /** Salt appended to the commitment when deriving the VK. */
    private static final byte[] VK_SALT =
            "EGDS_VK_SALT".getBytes(StandardCharsets.UTF_8);

    /** Number of random bytes used as the witness nonce. */
    private static final int NONCE_LENGTH_BYTES = 32;

    /** Source of cryptographic randomness for nonce generation. */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Encodes the given greeting message into a zero-knowledge proof.
     *
     * @param message the plaintext greeting to prove knowledge of
     * @return a {@link ZkpGreetingProof} containing the commitment
     *         and verification key
     */
    public ZkpGreetingProof encode(final String message) {
        byte[] nonce = new byte[NONCE_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(nonce);

        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] commitment = sha256(msgBytes, nonce);
        byte[] verificationKey = sha256(commitment, VK_SALT);

        String commitHex = HexFormat.of().formatHex(commitment);
        String vkHex = HexFormat.of().formatHex(verificationKey);

        LOG.info("[ZKP] proof generated commitment={}", commitHex);

        return new ZkpGreetingProof(commitHex, vkHex);
    }

    /**
     * Verifies the structural integrity of a ZKP proof without
     * reconstructing the original message.
     *
     * @param proof the proof to verify
     * @return {@code true} if the proof has non-null, non-empty
     *         commitment and verification key fields
     */
    public boolean verify(final ZkpGreetingProof proof) {
        return proof != null
                && proof.getCommitment() != null
                && !proof.getCommitment().isEmpty()
                && proof.getVerificationKey() != null
                && !proof.getVerificationKey().isEmpty();
    }

    /**
     * Computes SHA-256 over the concatenation of all supplied byte
     * arrays.
     *
     * @param parts byte arrays to hash in order
     * @return the 32-byte SHA-256 digest
     */
    private byte[] sha256(final byte[]... parts) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            for (byte[] part : parts) {
                md.update(part);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
