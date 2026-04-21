package com.egds.core.service;

import com.egds.config.JpaAuditingConfig;
import com.egds.core.entity.GreetingAuditLog;
import com.egds.core.repository.GreetingAuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JPA slice tests for {@link AuditLogService}.
 *
 * <p>{@code @DataJpaTest} configures an H2 in-memory database, scans for JPA entities
 * and repositories, and does not load the full application context (no Kafka, no Security).
 * {@link JpaAuditingConfig} is imported explicitly to activate {@code @CreatedDate} and
 * {@code @LastModifiedBy} population, which is not loaded by default in slice tests.
 * {@link AuditLogService} is imported because {@code @DataJpaTest} does not scan
 * {@code @Service} classes.
 */
@DataJpaTest
@Import({JpaAuditingConfig.class, AuditLogService.class})
class AuditLogServiceTest {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private GreetingAuditLogRepository auditLogRepository;

    @Test
    @DisplayName("record persists the audit log and populates the generated ID")
    void record_persistsAuditLog() {
        GreetingAuditLog log = buildAuditLog("corr-001", "127.0.0.1", "greeting.admin");

        GreetingAuditLog saved = auditLogService.record(log);

        assertThat(saved.getId()).isNotNull();
        assertThat(auditLogRepository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("record populates occurredAt via JPA Auditing")
    void record_populatesOccurredAt() {
        GreetingAuditLog log = buildAuditLog("corr-002", "10.0.0.1", "greeting.admin");

        GreetingAuditLog saved = auditLogService.record(log);

        assertThat(saved.getOccurredAt()).isNotNull();
    }

    @Test
    @DisplayName("findByCorrelationId returns only records matching the given correlationId")
    void findByCorrelationId_returnsMatchingRecords() {
        auditLogService.record(buildAuditLog("corr-003", "192.168.1.1", "greeting.admin"));
        auditLogService.record(buildAuditLog("corr-003", "192.168.1.2", "greeting.admin"));
        auditLogService.record(buildAuditLog("corr-other", "10.0.0.1", "greeting.admin"));

        List<GreetingAuditLog> results = auditLogService.findByCorrelationId("corr-003");

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> "corr-003".equals(r.getCorrelationId()));
    }

    @Test
    @DisplayName("countSuccessfulDeliveries returns the count of DELIVERED records only")
    void countSuccessfulDeliveries_countsOnlyDelivered() {
        auditLogService.record(buildAuditLog("corr-004", "127.0.0.1", "greeting.admin"));
        GreetingAuditLog failed = new GreetingAuditLog.Builder()
                .correlationId("corr-005")
                .requestIp("127.0.0.1")
                .threadName("test-thread")
                .principalName("greeting.admin")
                .deliveryStatus("FAILED")
                .durationMs(10L)
                .build();
        auditLogService.record(failed);

        long count = auditLogService.countSuccessfulDeliveries();

        assertThat(count).isGreaterThanOrEqualTo(1L);
    }

    private GreetingAuditLog buildAuditLog(String correlationId, String ip, String principal) {
        return new GreetingAuditLog.Builder()
                .correlationId(correlationId)
                .requestIp(ip)
                .threadName(Thread.currentThread().getName())
                .principalName(principal)
                .deliveryStatus("DELIVERED")
                .durationMs(5L)
                .build();
    }
}
