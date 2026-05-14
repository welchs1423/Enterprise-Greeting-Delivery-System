package com.egds.capitalism;

/**
 * Thrown when the subcontracted World-component provider fails to
 * fulfill its delivery obligation.
 *
 * <p>Callers should not interpret this as a headquarters fault.
 * All liability rests with the subcontractor.
 */
public class SubcontractorFailureException extends RuntimeException {

    /** Serial version UID for serialisation compatibility. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with the given detail message.
     *
     * @param message the failure detail message
     */
    public SubcontractorFailureException(final String message) {
        super(message);
    }
}
