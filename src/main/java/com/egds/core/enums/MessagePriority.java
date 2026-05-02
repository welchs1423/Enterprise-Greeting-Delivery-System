package com.egds.core.enums;

/**
 * Defines the priority levels for message delivery within the EGDS
 * pipeline. Priority affects queue ordering and SLA enforcement.
 */
public enum MessagePriority {

    /** Reserved for system-critical greetings requiring immediate delivery. */
    CRITICAL(0),

    /** High-priority greetings with expedited processing guarantees. */
    HIGH(1),

    /** Standard operational priority. Default for most payloads. */
    NORMAL(2),

    /** Best-effort delivery with no SLA commitment. */
    LOW(3);

    /** Numeric precedence value; lower values indicate higher precedence. */
    private final int level;

    MessagePriority(final int value) {
        this.level = value;
    }

    /**
     * Returns the numeric precedence value for this priority level.
     * Lower values indicate higher precedence.
     *
     * @return the priority level as an integer
     */
    public int getLevel() {
        return level;
    }
}
