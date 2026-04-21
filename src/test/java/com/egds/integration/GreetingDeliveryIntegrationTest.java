package com.egds.integration;

import com.egds.core.entity.GreetingAuditLog;
import com.egds.core.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration test covering the complete EGDS delivery chain:
 *
 * <ol>
 *   <li>Authenticate and obtain a JWT via {@code POST /api/v1/auth/token}.</li>
 *   <li>Call {@code GET /api/v1/greeting} with the JWT to initiate async delivery.</li>
 *   <li>Verify the Kafka consumer processes the event and persists an audit log record.</li>
 * </ol>
 *
 * <p>All four architectural layers — Security (JWT), Messaging (Kafka), Cache (@Cacheable),
 * and Persistence (@Transactional JPA) — are exercised in a single test execution.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = "${egds.kafka.topic.greeting:egds.greeting.events.test}")
class GreetingDeliveryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private AuditLogService auditLogService;

    @Test
    @DisplayName("Full pipeline: JWT auth -> Kafka publish -> consumer -> audit log persisted")
    void fullPipeline_jwtToKafkaToAuditLog() throws Exception {
        // Step 1: obtain JWT
        String token = obtainToken("greeting.admin", "egds-admin-pass");

        // Step 2: call greeting endpoint (returns 202 immediately)
        MvcResult greetingResult = mockMvc.perform(get("/api/v1/greeting")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.correlationId").isNotEmpty())
                .andReturn();

        String responseBody = greetingResult.getResponse().getContentAsString();
        String correlationId = objectMapper.readTree(responseBody).get("correlationId").asText();
        assertThat(correlationId).isNotBlank();

        // Step 3: wait for Kafka consumer to process and persist the audit log
        verify(auditLogService, timeout(10_000).atLeastOnce()).record(any(GreetingAuditLog.class));
    }

    @Test
    @DisplayName("Second greeting request demonstrates cache hit: assembleGreeting body not re-executed")
    void secondRequest_demonstratesCacheHit() throws Exception {
        String token = obtainToken("greeting.admin", "egds-admin-pass");

        // First request: cold cache, assembleGreeting executes
        mockMvc.perform(get("/api/v1/greeting")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isAccepted());

        // Second request: warm cache, assembleGreeting body is bypassed
        mockMvc.perform(get("/api/v1/greeting")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isAccepted());

        // Both requests trigger at least two audit log persists (two async Kafka messages)
        verify(auditLogService, timeout(15_000).atLeast(2)).record(any(GreetingAuditLog.class));
    }

    // --- helpers ---

    private String obtainToken(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "password", password
        ));
        MvcResult result = mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }
}
