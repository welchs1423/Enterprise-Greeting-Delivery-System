package com.egds.legacy;

import com.egds.core.exception.PaperJamException;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simulates a legacy dot-matrix printer by emitting each character of
 * the input to {@code System.out} at a fixed inter-character delay of
 * {@value #DEFAULT_CHAR_DELAY_MS} milliseconds.
 *
 * <p>A {@value #PAPER_JAM_PROBABILITY} probability jam event is
 * determined at invocation time.  When printing reaches the randomly
 * selected jam index, {@link PaperJamException} is thrown and output
 * is halted, simulating a mid-job paper jam.
 */
@Component
public class DotMatrixPrinterAdapter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(DotMatrixPrinterAdapter.class);

    /** Default inter-character print delay in milliseconds. */
    static final long DEFAULT_CHAR_DELAY_MS = 100L;

    /** Probability that a given print job encounters a paper jam. */
    static final double PAPER_JAM_PROBABILITY = 0.05;

    /** Random source used to determine jam occurrence and position. */
    private final Random random;

    /** Inter-character delay applied between each printed character. */
    private final long charDelayMs;

    /**
     * Constructs a {@code DotMatrixPrinterAdapter} with default settings.
     */
    public DotMatrixPrinterAdapter() {
        this.random = new Random();
        this.charDelayMs = DEFAULT_CHAR_DELAY_MS;
    }

    /**
     * Constructs a {@code DotMatrixPrinterAdapter} with injectable
     * randomness and delay, intended for deterministic unit testing.
     *
     * @param randomSource the random source to use for jam simulation
     * @param delayMs      inter-character delay in milliseconds
     */
    DotMatrixPrinterAdapter(final Random randomSource, final long delayMs) {
        this.random = randomSource;
        this.charDelayMs = delayMs;
    }

    /**
     * Prints each character of {@code text} to {@code System.out} with
     * a fixed inter-character delay, emulating dot-matrix print speed.
     *
     * <p>A paper-jam index is determined at invocation time with
     * probability {@value #PAPER_JAM_PROBABILITY}.  When printing
     * reaches that character index, {@link PaperJamException} is thrown
     * and the print job is aborted.
     *
     * @param text the string to print character by character
     * @throws PaperJamException if a paper jam condition is triggered
     */
    public void print(final String text) {
        int jamIndex = -1;
        if (!text.isEmpty()
                && random.nextDouble() < PAPER_JAM_PROBABILITY) {
            jamIndex = random.nextInt(text.length());
        }
        LOG.info("[PRINTER] Job started chars={}", text.length());
        for (int i = 0; i < text.length(); i++) {
            if (i == jamIndex) {
                LOG.warn("[PRINTER] Paper jam at index={}", i);
                throw new PaperJamException(
                        "Dot-matrix paper jam at index " + i + ".", i);
            }
            System.out.print(text.charAt(i));
            if (charDelayMs > 0) {
                try {
                    Thread.sleep(charDelayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        System.out.println();
        LOG.info("[PRINTER] Job complete");
    }
}
