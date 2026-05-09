package com.egds.core.exception;

/**
 * Thrown when the virtual dot-matrix printer encounters a paper jam
 * condition, halting the character-by-character print operation.
 */
public final class PaperJamException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Index of the character at which the jam was detected. */
    private final int characterIndex;

    /**
     * Constructs a new {@code PaperJamException}.
     *
     * @param message  description of the jam condition
     * @param charIdx  character index where the jam occurred
     */
    public PaperJamException(
            final String message,
            final int charIdx) {
        super(message);
        this.characterIndex = charIdx;
    }

    /**
     * Returns the index of the character at which the paper jam occurred.
     *
     * @return the character index
     */
    public int getCharacterIndex() {
        return characterIndex;
    }
}
