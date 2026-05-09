package com.egds.mycelial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulates broadcast of greeting data over the subterranean mycelial
 * network using glutamate chemical-signal frequency encoding.
 *
 * <p>Each byte of the UTF-8-encoded greeting is mapped to a glutamate
 * oscillation frequency in the biological signal range
 * [{@value #BASE_FREQ_HZ} Hz, ({@value #BASE_FREQ_HZ}
 * + {@value #FREQ_RANGE_HZ}) Hz].
 *
 * <p>In a production deployment these frequencies would be submitted to
 * a hardware transducer array interfacing with underground fungal networks.
 * In the current implementation the frequency sequence is computed and
 * emitted to the structured log only.
 *
 * <p>Frequency mapping: freq = BASE_FREQ_HZ + (byteValue / 255.0)
 * * FREQ_RANGE_HZ.
 */
@Component
public class MycelialNetworkAdapter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(MycelialNetworkAdapter.class);

    /** Minimum glutamate oscillation frequency in Hz. */
    static final double BASE_FREQ_HZ = 20.0;

    /** Frequency range mapped across the [0, 255] byte value space. */
    static final double FREQ_RANGE_HZ = 256.0;

    /** Mask to convert a signed byte to its unsigned integer value. */
    private static final int UNSIGNED_BYTE_MASK = 0xFF;

    /** Maximum unsigned byte value, used as the frequency divisor. */
    private static final double UNSIGNED_BYTE_MAX = 255.0;

    /**
     * Encodes {@code greetingText} as glutamate signal frequencies and
     * broadcasts to the mycelial network layer.
     *
     * @param correlationId request identifier for audit tracing
     * @param greetingText  text to encode; must not be null
     * @return ordered list of computed frequencies in Hz,
     *         one entry per UTF-8 byte of the input
     */
    public List<Double> broadcast(
            final String correlationId, final String greetingText) {
        byte[] bytes = greetingText.getBytes(StandardCharsets.UTF_8);
        List<Double> frequencies = encodeToFrequencies(bytes);
        LOG.info("[MYCELIAL] broadcast initiated correlationId={}"
                + " signal_count={} first_freq_hz={}",
                correlationId, frequencies.size(),
                frequencies.isEmpty() ? 0.0 : frequencies.get(0));
        LOG.debug("[MYCELIAL] full frequency sequence"
                + " correlationId={} frequencies={}",
                correlationId, frequencies);
        return frequencies;
    }

    /**
     * Maps each byte value to a glutamate oscillation frequency in Hz.
     *
     * @param bytes raw UTF-8 bytes of the greeting
     * @return ordered list of frequencies; one per input byte
     */
    List<Double> encodeToFrequencies(final byte[] bytes) {
        List<Double> result = new ArrayList<>(bytes.length);
        for (byte b : bytes) {
            int unsigned = b & UNSIGNED_BYTE_MASK;
            double freq = BASE_FREQ_HZ
                    + unsigned / UNSIGNED_BYTE_MAX * FREQ_RANGE_HZ;
            result.add(freq);
        }
        return result;
    }
}
