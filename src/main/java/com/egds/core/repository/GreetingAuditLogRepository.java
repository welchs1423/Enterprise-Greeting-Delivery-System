package com.egds.core.repository;

import com.egds.core.entity.GreetingAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for {@link GreetingAuditLog} persistence.
 * Provides standard CRUD operations and domain-specific query methods
 * for the EGDS audit trail. All write operations execute within the
 * transaction boundary established by the calling service layer.
 */
@Repository
public interface GreetingAuditLogRepository
        extends JpaRepository<GreetingAuditLog, Long> {

    /**
     * Retrieves all audit records for a specific correlation identifier,
     * ordered by creation timestamp ascending.
     *
     * @param correlationId the correlation identifier to search by
     * @return list of matching audit records
     */
    List<GreetingAuditLog> findByCorrelationIdOrderByOccurredAtAsc(
            String correlationId);

    /**
     * Retrieves all audit records within the specified time window.
     *
     * @param from the inclusive start of the time window
     * @param to   the inclusive end of the time window
     * @return list of matching audit records
     */
    List<GreetingAuditLog> findByOccurredAtBetween(
            LocalDateTime from, LocalDateTime to);

    /**
     * Counts the total number of records with {@code DELIVERED} status.
     *
     * @return count of successfully delivered greeting events
     */
    @Query("SELECT COUNT(g) FROM GreetingAuditLog g"
            + " WHERE g.deliveryStatus = 'DELIVERED'")
    long countSuccessfulDeliveries();
}
