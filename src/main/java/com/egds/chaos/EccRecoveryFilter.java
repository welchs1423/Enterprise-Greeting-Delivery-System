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
            int b = data[i] & 0xFF;
            result[i * 2] = (byte) encodeNibble((b >> 4) & 0xF);
            result[i * 2 + 1] = (byte) encodeNibble(b & 0xF);
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
            int high = decodeNibble(eccData[i * 2] & 0x7F);
            int low = decodeNibble(eccData[i * 2 + 1] & 0x7F);
            result[i] = (byte) ((high << 4) | low);
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
        int d1 = (nibble >> 3) & 1;
        int d2 = (nibble >> 2) & 1;
        int d3 = (nibble >> 1) & 1;
        int d4 = nibble & 1;
        int p1 = d1 ^ d2 ^ d4;
        int p2 = d1 ^ d3 ^ d4;
        int p4 = d2 ^ d3 ^ d4;
        return (p1 << 6) | (p2 << 5) | (d1 << 4)
            | (p4 << 3) | (d2 << 2) | (d3 << 1) | d4;
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
        int s1 = bit(corrected, 6) ^ bit(corrected, 4)
            ^ bit(corrected, 2) ^ bit(corrected, 0);
        int s2 = bit(corrected, 5) ^ bit(corrected, 4)
            ^ bit(corrected, 1) ^ bit(corrected, 0);
        int s4 = bit(corrected, 3) ^ bit(corrected, 2)
            ^ bit(corrected, 1) ^ bit(corrected, 0);
        int syndrome = (s4 << 2) | (s2 << 1) | s1;
        if (syndrome != 0) {
            corrected ^= (1 << (7 - syndrome));
        }
        return ((corrected >> 4) & 1) << 3
            | ((corrected >> 2) & 1) << 2
            | ((corrected >> 1) & 1) << 1
            | (corrected & 1);
    }

    private int bit(final int value, final int pos) {
        return (value >> pos) & 1;
    }
}
