package com.egds.nano;

/**
 * Contract for a single-character nano service in the letter mesh.
 *
 * <p>Each implementation handles exactly one character position in
 * the decomposed delivery string, enabling per-character scatter
 * processing across the nano service mesh.</p>
 */
@FunctionalInterface
public interface LetterNanoService {

    /**
     * Processes a single character and returns the result.
     *
     * @param letter the character to process
     * @return the processed character
     */
    char process(char letter);
}
