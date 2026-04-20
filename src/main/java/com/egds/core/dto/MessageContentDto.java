package com.egds.core.dto;

import com.egds.core.enums.MessagePriority;
import java.time.Instant;

/**
 * Data Transfer Object encapsulating the raw greeting message payload.
 * Instances are immutable and constructed exclusively via the nested {@link Builder}.
 * This class serves as the canonical input contract for the EGDS delivery pipeline.
 */
public final class MessageContentDto {

    private final String content;
    private final String locale;
    private final MessagePriority priority;
    private final String correlationId;
    private final long timestamp;

    private MessageContentDto(Builder builder) {
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
    public String getContent() { return content; }

    /**
     * Returns the BCP 47 locale tag associated with this message.
     *
     * @return the locale string (e.g., "en-US")
     */
    public String getLocale() { return locale; }

    /**
     * Returns the delivery priority assigned to this message payload.
     *
     * @return the {@link MessagePriority} value
     */
    public MessagePriority getPriority() { return priority; }

    /**
     * Returns the unique correlation identifier for distributed tracing.
     *
     * @return the correlation ID string
     */
    public String getCorrelationId() { return correlationId; }

    /**
     * Returns the epoch millisecond timestamp at which this DTO was constructed.
     *
     * @return the creation timestamp in milliseconds since epoch
     */
    public long getTimestamp() { return timestamp; }

    /**
     * Builder for constructing immutable {@link MessageContentDto} instances.
     * Enforces mandatory field presence prior to {@link #build()} invocation.
     */
    public static final class Builder {

        private final String content;
        private final String correlationId;
        private String locale = "en-US";
        private MessagePriority priority = MessagePriority.NORMAL;
        private final long timestamp;

        /**
         * Initializes the Builder with mandatory content and correlation identifier fields.
         *
         * @param content       the raw message text; must not be null or empty
         * @param correlationId the unique trace identifier; must not be null
         */
        public Builder(String content, String correlationId) {
            this.content = content;
            this.correlationId = correlationId;
            this.timestamp = Instant.now().toEpochMilli();
        }

        /**
         * Sets the BCP 47 locale tag for this message payload.
         *
         * @param locale the locale string; defaults to "en-US" if not specified
         * @return this Builder instance for method chaining
         */
        public Builder locale(String locale) {
            this.locale = locale;
            return this;
        }

        /**
         * Sets the delivery priority for this message payload.
         *
         * @param priority the {@link MessagePriority}; defaults to NORMAL if not specified
         * @return this Builder instance for method chaining
         */
        public Builder priority(MessagePriority priority) {
            this.priority = priority;
            return this;
        }

        /**
         * Constructs and returns the immutable {@link MessageContentDto} instance.
         *
         * @return a fully populated {@link MessageContentDto}
         * @throws IllegalStateException if mandatory fields are null or empty
         */
        public MessageContentDto build() {
            if (content == null || content.isEmpty()) {
                throw new IllegalStateException("MessageContentDto requires non-empty content.");
            }
            if (correlationId == null || correlationId.isEmpty()) {
                throw new IllegalStateException("MessageContentDto requires a non-null correlationId.");
            }
            return new MessageContentDto(this);
        }
    }

    @Override
    public String toString() {
        return "MessageContentDto{" +
               "correlationId='" + correlationId + '\'' +
               ", content='" + content + '\'' +
               ", locale='" + locale + '\'' +
               ", priority=" + priority +
               ", timestamp=" + timestamp +
               '}';
    }
}
