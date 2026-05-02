package com.egds.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JPA entity representing a single greeting delivery audit record.
 * One record is persisted for every pipeline execution triggered by a
 * Kafka consumer event.
 *
 * <p>JPA Auditing populates {@link #occurredAt} via {@code @CreatedDate}
 * and {@link #auditedBy} via {@code @LastModifiedBy}, sourced from the
 * configured {@code AuditorAware} bean at record creation time.
 *
 * <p>The sequence generator targets Oracle Database semantics. H2 in
 * Oracle-compatibility mode ({@code MODE=Oracle}) supports sequences
 * natively, making this configuration portable across both environments.
 */
@Entity
@Table(name = "GREETING_AUDIT_LOG")
@EntityListeners(AuditingEntityListener.class)
public class GreetingAuditLog {

    /** Sequence allocation size for the greeting audit PK generator. */
    private static final int SEQ_ALLOC_SIZE = 50;

    /** Maximum column length for the correlation ID field. */
    private static final int CORR_ID_LENGTH = 64;

    /** Maximum column length for the request IP field (covers IPv6). */
    private static final int IP_LENGTH = 45;

    /** Maximum column length for name-type string fields. */
    private static final int NAME_LENGTH = 128;

    /** Maximum column length for the delivery status field. */
    private static final int STATUS_LENGTH = 32;

    /** Database-generated primary key. */
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "greeting_audit_seq")
    @SequenceGenerator(
            name = "greeting_audit_seq",
            sequenceName = "SEQ_GREETING_AUDIT",
            allocationSize = SEQ_ALLOC_SIZE
    )
    @Column(name = "ID")
    private Long id;

    /** Correlation ID linking this record to the originating event. */
    @Column(name = "CORRELATION_ID", nullable = false,
            length = CORR_ID_LENGTH)
    private String correlationId;

    /** IPv4 or IPv6 address of the originating client. */
    @Column(name = "REQUEST_IP", length = IP_LENGTH)
    private String requestIp;

    /** JVM thread name on which the Kafka consumer ran the pipeline. */
    @Column(name = "THREAD_NAME", length = NAME_LENGTH)
    private String threadName;

    /** Authenticated principal name from the originating JWT token. */
    @Column(name = "PRINCIPAL_NAME", length = NAME_LENGTH)
    private String principalName;

    /** Terminal delivery status of the pipeline (DELIVERED or FAILED). */
    @Column(name = "DELIVERY_STATUS", length = STATUS_LENGTH)
    private String deliveryStatus;

    /** Pipeline execution wall-clock duration in milliseconds. */
    @Column(name = "DURATION_MS")
    private Long durationMs;

    /**
     * Timestamp at which this audit record was created.
     * Populated automatically by JPA Auditing; must not be set manually.
     */
    @CreatedDate
    @Column(name = "OCCURRED_AT", nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    /**
     * Principal name recorded by JPA Auditing at record creation time.
     * Sourced from {@code AuditorAware}; reflects the consumer thread.
     */
    @LastModifiedBy
    @Column(name = "AUDITED_BY", length = NAME_LENGTH)
    private String auditedBy;

    /** Required by JPA specification; not for direct use. */
    protected GreetingAuditLog() {
    }

    private GreetingAuditLog(final Builder builder) {
        this.correlationId = builder.correlationId;
        this.requestIp = builder.requestIp;
        this.threadName = builder.threadName;
        this.principalName = builder.principalName;
        this.deliveryStatus = builder.deliveryStatus;
        this.durationMs = builder.durationMs;
    }

    /**
     * Returns the database-generated primary key.
     *
     * @return the ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the correlation ID of the originating delivery event.
     *
     * @return the correlation ID string
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Returns the originating client IP address.
     *
     * @return the request IP string
     */
    public String getRequestIp() {
        return requestIp;
    }

    /**
     * Returns the name of the consumer thread that ran the pipeline.
     *
     * @return the thread name string
     */
    public String getThreadName() {
        return threadName;
    }

    /**
     * Returns the authenticated principal name from the JWT token.
     *
     * @return the principal name string
     */
    public String getPrincipalName() {
        return principalName;
    }

    /**
     * Returns the terminal delivery status of the pipeline execution.
     *
     * @return the delivery status string
     */
    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    /**
     * Returns the pipeline execution duration in milliseconds.
     *
     * @return the duration in milliseconds
     */
    public Long getDurationMs() {
        return durationMs;
    }

    /**
     * Returns the timestamp at which this record was created.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    /**
     * Returns the principal name recorded by JPA Auditing.
     *
     * @return the audited-by principal string
     */
    public String getAuditedBy() {
        return auditedBy;
    }

    /** Builder for {@link GreetingAuditLog}. */
    public static final class Builder {

        /** Correlation ID of the originating delivery event. */
        private String correlationId;

        /** IPv4 or IPv6 address of the originating client. */
        private String requestIp;

        /** JVM thread name of the Kafka consumer. */
        private String threadName;

        /** Authenticated principal name from the JWT token. */
        private String principalName;

        /** Terminal delivery status string. */
        private String deliveryStatus;

        /** Pipeline execution duration in milliseconds. */
        private Long durationMs;

        /**
         * Sets the correlation ID.
         *
         * @param v the correlation ID value
         * @return this Builder
         */
        public Builder correlationId(final String v) {
            this.correlationId = v;
            return this;
        }

        /**
         * Sets the request IP address.
         *
         * @param v the request IP value
         * @return this Builder
         */
        public Builder requestIp(final String v) {
            this.requestIp = v;
            return this;
        }

        /**
         * Sets the thread name.
         *
         * @param v the thread name value
         * @return this Builder
         */
        public Builder threadName(final String v) {
            this.threadName = v;
            return this;
        }

        /**
         * Sets the principal name.
         *
         * @param v the principal name value
         * @return this Builder
         */
        public Builder principalName(final String v) {
            this.principalName = v;
            return this;
        }

        /**
         * Sets the delivery status.
         *
         * @param v the delivery status value
         * @return this Builder
         */
        public Builder deliveryStatus(final String v) {
            this.deliveryStatus = v;
            return this;
        }

        /**
         * Sets the duration in milliseconds.
         *
         * @param v the duration value
         * @return this Builder
         */
        public Builder durationMs(final Long v) {
            this.durationMs = v;
            return this;
        }

        /**
         * Builds and returns a new {@link GreetingAuditLog}.
         *
         * @return a new GreetingAuditLog instance
         */
        public GreetingAuditLog build() {
            return new GreetingAuditLog(this);
        }
    }
}
