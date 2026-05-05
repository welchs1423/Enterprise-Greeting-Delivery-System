package com.egds.chaos;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EccRecoveryFilter} Hamming(7,4) codec.
 */
class EccRecoveryFilterTest {

    private EccRecoveryFilter filter;

    /** Initialises a fresh filter before each test. */
    @BeforeEach
    void setUp() {
        filter = new EccRecoveryFilter();
    }

    /** Verifies lossless encode-decode round-trip for a typical string. */
    @Test
    void encodeDecodeLossless() {
        String original = "Hello, World!";
        byte[] encoded = filter.encodeString(original);
        String recovered = filter.decodeString(encoded);
        assertThat(recovered).isEqualTo(original);
    }

    /** Verifies that a single flipped bit in ECC data is corrected. */
    @Test
    void singleBitFlipRecovery() {
        byte[] original = "Hello".getBytes();
        byte[] encoded = filter.encode(original);
        encoded[0] ^= 0x01;
        byte[] decoded = filter.decode(encoded);
        assertThat(decoded).isEqualTo(original);
    }

    /** Verifies round-trip for all 16 possible nibble values. */
    @Test
    void encodeNibbleRoundTrip() {
        for (int nibble = 0; nibble < 16; nibble++) {
            int codeword = filter.encodeNibble(nibble);
            int decoded = filter.decodeNibble(codeword);
            assertThat(decoded)
                .as("nibble %d roundtrip", nibble)
                .isEqualTo(nibble);
        }
    }

    /** Verifies single-bit correction at every bit position for all nibbles. */
    @Test
    void singleBitFlipInEachPosition() {
        for (int nibble = 0; nibble < 16; nibble++) {
            int codeword = filter.encodeNibble(nibble);
            for (int bit = 0; bit < 7; bit++) {
                int corrupted = codeword ^ (1 << bit);
                int recovered = filter.decodeNibble(corrupted);
                assertThat(recovered)
                    .as("nibble=%d bit=%d", nibble, bit)
                    .isEqualTo(nibble);
            }
        }
    }

    /** Verifies that encode doubles the byte count and decode halves it. */
    @Test
    void encodedSizeIsDouble() {
        byte[] data = new byte[10];
        byte[] encoded = filter.encode(data);
        assertThat(encoded).hasSize(20);
        byte[] decoded = filter.decode(encoded);
        assertThat(decoded).hasSize(10);
    }
}
