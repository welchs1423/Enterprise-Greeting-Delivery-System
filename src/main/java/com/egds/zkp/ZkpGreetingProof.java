package com.egds.zkp;

/**
 * Immutable proof record produced by {@link ZkpGreetingEncoder}.
 *
 * <p>Contains a commitment hash and a verification key that together
 * constitute a mock zero-knowledge proof of greeting knowledge.
 * Neither field reveals the plaintext greeting message.
 */
public final class ZkpGreetingProof {

    /** Hex-encoded SHA-256 commitment: H(message || nonce). */
    private final String commitment;

    /** Hex-encoded SHA-256 verification key: H(commitment || salt). */
    private final String verificationKey;

    /**
     * Constructs a new proof with the supplied commitment and
     * verification key.
     *
     * @param proofCommitment      hex-encoded SHA-256 commitment hash
     * @param proofVerificationKey hex-encoded SHA-256 verification key
     */
    public ZkpGreetingProof(
            final String proofCommitment,
            final String proofVerificationKey) {
        this.commitment = proofCommitment;
        this.verificationKey = proofVerificationKey;
    }

    /**
     * Returns the hex-encoded SHA-256 commitment hash.
     *
     * @return commitment hash string
     */
    public String getCommitment() {
        return commitment;
    }

    /**
     * Returns the hex-encoded SHA-256 verification key.
     *
     * @return verification key string
     */
    public String getVerificationKey() {
        return verificationKey;
    }
}
