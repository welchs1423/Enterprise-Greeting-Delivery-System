package com.egds.core.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JPA entity representing a single greeting delivery audit record.
 * One record is persisted for every pipeline execution triggered by a Kafka consumer event.
 *
 * <p>JPA Auditing populates {@link #occurredAt} via {@code @CreatedDate} and
 * {@link #auditedBy} via {@code @LastModifiedBy}, sourced from the configured
 * {@code AuditorAware} bean at the time of record creation.
 *
 * <p>The sequence generator ({@code SEQ_GREETING_AUDIT}) targets Oracle Database semantics.
 * H2 in Oracle-compatibility mode ({@code MODE=Oracle}) supports sequences natively,
 * making this configuration portable across both environments without modification.
 */
@Entity
@Table(name = "GREETING_AUDIT_LOG")
@EntityListeners(AuditingEntityListener.class)
public class GreetingAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "greeting_audit_seq")
    @SequenceGenerator(
            name = "greeting_audit_seq",
            sequenceName = "SEQ_GREETING_AUDIT",
            allocationSize = 50
    )
    @Column(name = "ID")
    private Long id;

    /** Correlation identifier linking this record to the originating Kafka delivery event. */
    @Column(name = "CORRELATION_ID", nullable = false, length = 64)
    private String correlationId;

    /** IPv4 or IPv6 address of the client that initiated the greeting request. */
    @Column(name = "REQUEST_IP", length = 45)
    private String requestIp;

    /** Name of the JVM thread on which the Kafka consumer executed the pipeline. */
    @Column(name = "THREAD_NAME", length = 128)
    private String threadName;

    /** Authenticated principal name resolved from the JWT token of the originating request. */
    @Column(name = "PRINCIPAL_NAME", length = 128)
    private String principalName;

    /** Terminal delivery status of the pipeline execution (DELIVERED or FAILED). */
    @Column(name = "DELIVERY_STATUS", length = 32)
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
     * Principal name recorded by the JPA Auditing infrastructure at record creation time.
     * Sourced from {@code AuditorAware}; reflects the Kafka consumer thread context.
     */
    @LastModifiedBy
    @Column(name = "AUDITED_BY", length = 128)
    private String auditedBy;

    /** Required by JPA specification; not for direct use. */
    protected GreetingAuditLog() {
    }

    private GreetingAuditLog(Builder builder) {
        this.correlationId = builder.correlationId;
        this.requestIp = builder.requestIp;
        this.threadName = builder.threadName;
        this.principalName = builder.principalName;
        this.deliveryStatus = builder.deliveryStatus;
        this.durationMs = builder.durationMs;
    }

    public Long getId() { return id; }
    public String getCorrelationId() { return correlationId; }
    public String getRequestIp() { return requestIp; }
    public String getThreadName() { return threadName; }
    public String getPrincipalName() { return principalName; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public Long getDurationMs() { return durationMs; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public String getAuditedBy() { return auditedBy; }

    /** Builder for {@link GreetingAuditLog}. */
    public static final class Builder {
        private String correlationId;
        private String requestIp;
        private String threadName;
        private String principalName;
        private String deliveryStatus;
        private Long durationMs;

        public Builder correlationId(String v) { this.correlationId = v; return this; }
        public Builder requestIp(String v) { this.requestIp = v; return this; }
        public Builder threadName(String v) { this.threadName = v; return this; }
        public Builder principalName(String v) { this.principalName = v; return this; }
        public Builder deliveryStatus(String v) { this.deliveryStatus = v; return this; }
        public Builder durationMs(Long v) { this.durationMs = v; return this; }

        public GreetingAuditLog build() { return new GreetingAuditLog(this); }
    }
}
