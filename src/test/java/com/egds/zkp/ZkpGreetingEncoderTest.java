package com.egds.zkp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ZkpGreetingEncoder}.
 */
class ZkpGreetingEncoderTest {

    /** Encoder under test. */
    private ZkpGreetingEncoder encoder;

    /** Creates a fresh encoder instance before each test. */
    @BeforeEach
    void setUp() {
        encoder = new ZkpGreetingEncoder();
    }

    /** Verifies that encode returns a non-null proof. */
    @Test
    void encode_returnsNonNullProof() {
        ZkpGreetingProof proof = encoder.encode("Hello, World!");
        assertThat(proof).isNotNull();
    }

    /** Verifies that the commitment field is a 64-char hex string. */
    @Test
    void encode_commitmentIsHex64Chars() {
        ZkpGreetingProof proof = encoder.encode("Hello, World!");
        assertThat(proof.getCommitment())
                .isNotNull()
                .hasSize(64)
                .matches("[0-9a-f]+");
    }

    /** Verifies that the verification key field is a 64-char hex string. */
    @Test
    void encode_verificationKeyIsHex64Chars() {
        ZkpGreetingProof proof = encoder.encode("Hello, World!");
        assertThat(proof.getVerificationKey())
                .isNotNull()
                .hasSize(64)
                .matches("[0-9a-f]+");
    }

    /**
     * Verifies that two proofs for the same message produce different
     * commitments due to distinct random nonces.
     */
    @RepeatedTest(5)
    void encode_sameMessageProducesDifferentCommitments() {
        ZkpGreetingProof first = encoder.encode("Hello, World!");
        ZkpGreetingProof second = encoder.encode("Hello, World!");
        assertThat(first.getCommitment())
                .isNotEqualTo(second.getCommitment());
    }

    /** Verifies that a valid proof passes structural verification. */
    @Test
    void verify_validProofReturnsTrue() {
        ZkpGreetingProof proof = encoder.encode("Hello, World!");
        assertThat(encoder.verify(proof)).isTrue();
    }

    /** Verifies that a null proof fails structural verification. */
    @Test
    void verify_nullProofReturnsFalse() {
        assertThat(encoder.verify(null)).isFalse();
    }

    /** Verifies that a proof with an empty commitment fails verification. */
    @Test
    void verify_emptyCommitmentReturnsFalse() {
        ZkpGreetingProof proof = new ZkpGreetingProof("", "abcd1234");
        assertThat(encoder.verify(proof)).isFalse();
    }
}
