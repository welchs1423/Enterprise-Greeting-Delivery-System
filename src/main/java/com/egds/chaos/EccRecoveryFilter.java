package com.egds.chaos;

import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

/**
 * Hamming(7,4) error-correcting code implementation.
 *
 * <p>Each 4-bit nibble is encoded as a 7-bit Hamming codeword.
 * Bit layout (bits 6..0): p1 p2 d1 p4 d2 d3 d4.
 * A single-bit error per codeword is detected and corrected on decode.
 *
 * <p>Encoding doubles the byte count (each input byte yields two ECC
 * bytes, one per nibble). Used by {@link com.egds.ipfs.IpfsGreetingResolver}
 * to protect stored content against cosmic-ray bit flips injected by
 * {@link CosmicRaySimulator}.
 */
@Component
public class EccRecoveryFilter {

    /** Mask to extract an unsigned byte value. */
    private static final int BYTE_MASK = 0xFF;

    /** Number of bits in a nibble. */
    private static final int NIBBLE_BITS = 4;

    /** Mask for the lower nibble of a byte. */
    private static final int NIBBLE_MASK = 0x0F;

    /** Mask for a 7-bit Hamming codeword. */
    private static final int HAMMING_MASK = 0x7F;

    /** Bit position of data bit d1 within an input nibble. */
    private static final int D1_NIBBLE_BIT = 3;

    /** Bit position of parity p1 within the 7-bit codeword. */
    private static final int P1_BIT = 6;

    /** Bit position of parity p2 within the 7-bit codeword. */
    private static final int P2_BIT = 5;

    /** Bit position of data d1 within the 7-bit codeword. */
    private static final int D1_BIT = 4;

    /** Bit position of parity p4 within the 7-bit codeword. */
    private static final int P4_BIT = 3;

    /** Total bits in a Hamming(7,4) codeword. */
    private static final int CODEWORD_BITS = 7;

    /**
     * Encodes a byte array using Hamming(7,4).
     * Each input byte produces two ECC bytes (one per nibble).
     *
     * @param data raw input bytes
     * @return ECC-encoded bytes with length {@code data.length * 2}
     */
    public byte[] encode(final byte[] data) {
        byte[] result = new byte[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            int b = data[i] & BYTE_MASK;
            result[i * 2] =
                    (byte) encodeNibble((b >> NIBBLE_BITS) & NIBBLE_MASK);
            result[i * 2 + 1] =
                    (byte) encodeNibble(b & NIBBLE_MASK);
        }
        return result;
    }

    /**
     * Decodes an ECC-encoded byte array, correcting single-bit errors.
     *
     * @param eccData ECC-encoded bytes (must have even length)
     * @return decoded bytes with length {@code eccData.length / 2}
     */
    public byte[] decode(final byte[] eccData) {
        byte[] result = new byte[eccData.length / 2];
        for (int i = 0; i < result.length; i++) {
            int high =
                    decodeNibble(eccData[i * 2] & HAMMING_MASK);
            int low =
                    decodeNibble(eccData[i * 2 + 1] & HAMMING_MASK);
            result[i] = (byte) ((high << NIBBLE_BITS) | low);
        }
        return result;
    }

    /**
     * Encodes a string to ECC-encoded bytes using UTF-8.
     *
     * @param content input string
     * @return ECC-encoded bytes
     */
    public byte[] encodeString(final String content) {
        return encode(content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes ECC-encoded bytes to a string, correcting single-bit errors.
     *
     * @param eccData ECC-encoded bytes
     * @return decoded string
     */
    public String decodeString(final byte[] eccData) {
        return new String(decode(eccData), StandardCharsets.UTF_8);
    }

    /**
     * Encodes a 4-bit nibble as a 7-bit Hamming(7,4) codeword.
     * Parity equations: p1=d1^d2^d4, p2=d1^d3^d4, p4=d2^d3^d4.
     *
     * @param nibble 4-bit input value (0-15)
     * @return 7-bit codeword (bits 6..0)
     */
    int encodeNibble(final int nibble) {
        int d1 = (nibble >> D1_NIBBLE_BIT) & 1;
        int d2 = (nibble >> 2) & 1;
        int d3 = (nibble >> 1) & 1;
        int d4 = nibble & 1;
        int p1 = d1 ^ d2 ^ d4;
        int p2 = d1 ^ d3 ^ d4;
        int p4 = d2 ^ d3 ^ d4;
        return (p1 << P1_BIT) | (p2 << P2_BIT) | (d1 << D1_BIT)
            | (p4 << P4_BIT) | (d2 << 2) | (d3 << 1) | d4;
    }

    /**
     * Decodes a 7-bit Hamming(7,4) codeword with single-bit correction.
     * Syndrome s = s4*4+s2*2+s1 identifies the error bit position (1-7).
     *
     * @param codeword 7-bit Hamming codeword
     * @return corrected 4-bit nibble value
     */
    int decodeNibble(final int codeword) {
        int corrected = codeword;
        int s1 = bit(corrected, P1_BIT) ^ bit(corrected, D1_BIT)
            ^ bit(corrected, 2) ^ bit(corrected, 0);
        int s2 = bit(corrected, P2_BIT) ^ bit(corrected, D1_BIT)
            ^ bit(corrected, 1) ^ bit(corrected, 0);
        int s4 = bit(corrected, P4_BIT) ^ bit(corrected, 2)
            ^ bit(corrected, 1) ^ bit(corrected, 0);
        int syndrome = (s4 << 2) | (s2 << 1) | s1;
        if (syndrome != 0) {
            corrected ^= (1 << (CODEWORD_BITS - syndrome));
        }
        return ((corrected >> D1_BIT) & 1) << D1_NIBBLE_BIT
            | ((corrected >> 2) & 1) << 2
            | ((corrected >> 1) & 1) << 1
            | (corrected & 1);
    }

    private int bit(final int value, final int pos) {
        return (value >> pos) & 1;
    }
}
