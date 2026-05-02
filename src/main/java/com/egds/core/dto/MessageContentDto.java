package com.egds.core.dto;

import com.egds.core.enums.MessagePriority;
import java.time.Instant;

/**
 * Data Transfer Object encapsulating the raw greeting message payload.
 * Instances are immutable and constructed exclusively via the nested
 * {@link Builder}. Serves as the canonical input contract for the EGDS
 * delivery pipeline.
 */
public final class MessageContentDto {

    /** Raw greeting message content. */
    private final String content;

    /** BCP 47 locale tag associated with this message. */
    private final String locale;

    /** Delivery priority assigned to this message payload. */
    private final MessagePriority priority;

    /** Unique correlation identifier for distributed tracing. */
    private final String correlationId;

    /** Epoch millisecond timestamp at which this DTO was constructed. */
    private final long timestamp;

    private MessageContentDto(final Builder builder) {
        this.content = builder.content;
        this.locale = builder.locale;
        this.priority = builder.priority;
        this.correlationId = builder.correlationId;
        this.timestamp = builder.timestamp;
    }

    /**
     * Returns the raw greeting message content.
     *
     * @return the message content string
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the BCP 47 locale tag associated with this message.
     *
     * @return the locale string (e.g., "en-US")
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Returns the delivery priority assigned to this message payload.
     *
     * @return the {@link MessagePriority} value
     */
    public MessagePriority getPriority() {
        return priority;
    }

    /**
     * Returns the unique correlation identifier for distributed tracing.
     *
     * @return the correlation ID string
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Returns the epoch millisecond timestamp at which this DTO was
     * constructed.
     *
     * @return the creation timestamp in milliseconds since epoch
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Builder for constructing immutable {@link MessageContentDto}
     * instances. Enforces mandatory field presence prior to
     * {@link #build()} invocation.
     */
    public static final class Builder {

        /** Raw message text; mandatory. */
        private final String content;

        /** Unique trace identifier; mandatory. */
        private final String correlationId;

        /** BCP 47 locale tag; defaults to "en-US". */
        private String locale = "en-US";

        /** Delivery priority; defaults to NORMAL. */
        private MessagePriority priority = MessagePriority.NORMAL;

        /** Construction timestamp in epoch milliseconds. */
        private final long timestamp;

        /**
         * Initializes the Builder with mandatory content and correlation
         * identifier fields.
         *
         * @param rawContent the raw message text; must not be null
         * @param corrId     the unique trace identifier; must not be null
         */
        public Builder(final String rawContent, final String corrId) {
            this.content = rawContent;
            this.correlationId = corrId;
            this.timestamp = Instant.now().toEpochMilli();
        }

        /**
         * Sets the BCP 47 locale tag for this message payload.
         *
         * @param localeValue the locale string; defaults to "en-US"
         * @return this Builder instance for method chaining
         */
        public Builder locale(final String localeValue) {
            this.locale = localeValue;
            return this;
        }

        /**
         * Sets the delivery priority for this message payload.
         *
         * @param priorityValue the {@link MessagePriority}
         * @return this Builder instance for method chaining
         */
        public Builder priority(final MessagePriority priorityValue) {
            this.priority = priorityValue;
            return this;
        }

        /**
         * Constructs and returns the immutable {@link MessageContentDto}.
         *
         * @return a fully populated {@link MessageContentDto}
         * @throws IllegalStateException if mandatory fields are null/empty
         */
        public MessageContentDto build() {
            if (content == null || content.isEmpty()) {
                throw new IllegalStateException(
                        "MessageContentDto requires non-empty content.");
            }
            if (correlationId == null || correlationId.isEmpty()) {
                throw new IllegalStateException(
                        "MessageContentDto requires a non-null"
                        + " correlationId.");
            }
            return new MessageContentDto(this);
        }
    }

    @Override
    public String toString() {
        return "MessageContentDto{"
                + "correlationId='" + correlationId + '\''
                + ", content='" + content + '\''
                + ", locale='" + locale + '\''
                + ", priority=" + priority
                + ", timestamp=" + timestamp
                + '}';
    }
}
