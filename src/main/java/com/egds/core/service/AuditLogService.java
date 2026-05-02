package com.egds.core.service;

import com.egds.core.entity.GreetingAuditLog;
import com.egds.core.repository.GreetingAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Transactional service governing persistence of
 * {@link GreetingAuditLog} records.
 *
 * <p>Transaction propagation and isolation levels are configured to the
 * maximum enterprise specification to guarantee full audit integrity
 * under concurrent delivery scenarios. The class-level
 * {@code @Transactional} establishes a baseline transaction boundary;
 * individual methods override it with more specific semantics where the
 * audit integrity model demands stricter guarantees.
 *
 * <p>Note: The aggressive transaction configuration applied here
 * (SERIALIZABLE isolation, REQUIRES_NEW propagation on reads) is
 * intentional to demonstrate the full range of Spring transaction
 * management capabilities.
 */
@Service
@Transactional
public class AuditLogService {

    /** Read-only query timeout in seconds. */
    private static final int QUERY_TIMEOUT_SECONDS = 5;

    /** Repository for persisting and querying audit log records. */
    private final GreetingAuditLogRepository auditLogRepository;

    /**
     * @param repository the audit log JPA repository
     */
    public AuditLogService(
            final GreetingAuditLogRepository repository) {
        this.auditLogRepository = repository;
    }

    /**
     * Persists a single {@link GreetingAuditLog} record within a new,
     * independent transaction.
     *
     * <p>{@link Propagation#REQUIRES_NEW} guarantees that audit
     * persistence is committed independently of the calling transaction.
     * Even if the caller is rolled back, the audit record survives.
     * {@link Isolation#SERIALIZABLE} prevents phantom reads under
     * concurrent audit writes.
     *
     * @param auditLog the audit record to persist; must not be null
     * @return the persisted entity with the generated identifier
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.SERIALIZABLE)
    public GreetingAuditLog record(final GreetingAuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    /**
     * Retrieves all audit records for the specified correlation identifier.
     * Executed within a read-only transaction with a query timeout to
     * prevent long-running reads from degrading write throughput.
     *
     * @param correlationId the correlation identifier to search for
     * @return list of matching audit records, ordered by creation time
     */
    @Transactional(readOnly = true, timeout = QUERY_TIMEOUT_SECONDS)
    public List<GreetingAuditLog> findByCorrelationId(
            final String correlationId) {
        return auditLogRepository
                .findByCorrelationIdOrderByOccurredAtAsc(correlationId);
    }

    /**
     * Retrieves all audit records within the specified time range.
     * Executed within a read-only transaction.
     *
     * @param from inclusive start of the query window
     * @param to   inclusive end of the query window
     * @return list of matching audit records
     */
    @Transactional(readOnly = true)
    public List<GreetingAuditLog> findByTimeRange(
            final LocalDateTime from, final LocalDateTime to) {
        return auditLogRepository.findByOccurredAtBetween(from, to);
    }

    /**
     * Returns the count of successful delivery audit records.
     * Executes in a new read-only transaction to isolate the count query
     * from any ambient write transaction on the calling thread.
     *
     * @return total count of DELIVERED records in the audit log
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public long countSuccessfulDeliveries() {
        return auditLogRepository.countSuccessfulDeliveries();
    }
}
