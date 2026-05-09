package com.egds.core.exception;

/**
 * Thrown when the AI board of directors does not reach unanimous
 * approval for a greeting delivery request.
 */
public final class BoardRejectionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Correlation ID of the delivery cycle that was rejected. */
    private final String correlationId;

    /** Name of the board member who cast the dissenting vote. */
    private final String rejectingMember;

    /**
     * Constructs a new {@code BoardRejectionException}.
     *
     * @param message   description of the rejection
     * @param corrId    delivery cycle identifier
     * @param rejMember name of the member who rejected
     */
    public BoardRejectionException(
            final String message,
            final String corrId,
            final String rejMember) {
        super(message);
        this.correlationId = corrId;
        this.rejectingMember = rejMember;
    }

    /**
     * Returns the correlation ID of the rejected delivery cycle.
     *
     * @return the correlation ID string
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Returns the name of the board member who rejected the proposal.
     *
     * @return the rejecting member name
     */
    public String getRejectingMember() {
        return rejectingMember;
    }
}
