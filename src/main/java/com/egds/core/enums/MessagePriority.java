package com.egds.core.enums;

/**
 * Defines the priority levels for message delivery within the EGDS pipeline.
 * Priority affects queue ordering and SLA enforcement in the delivery lifecycle.
 */
public enum MessagePriority {

    /** Reserved for system-critical greetings requiring immediate delivery. */
    CRITICAL(0),

    /** High-priority greetings with expedited processing guarantees. */
    HIGH(1),

    /** Standard operational priority. Default for most greeting payloads. */
    NORMAL(2),

    /** Best-effort delivery with no SLA commitment. */
    LOW(3);

    private final int level;

    MessagePriority(int level) {
        this.level = level;
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
