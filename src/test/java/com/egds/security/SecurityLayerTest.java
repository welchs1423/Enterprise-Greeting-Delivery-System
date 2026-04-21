package com.egds.security;

import com.egds.messaging.GreetingEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the EGDS security layer.
 *
 * <p>Verifies that:
 * <ul>
 *   <li>The token endpoint issues a JWT for valid credentials.</li>
 *   <li>The greeting endpoint rejects unauthenticated requests with HTTP 401.</li>
 *   <li>The greeting endpoint rejects requests with an invalid JWT with HTTP 401.</li>
 *   <li>The greeting endpoint rejects valid JWTs lacking {@code ROLE_GREETING_ADMIN} with HTTP 403.</li>
 *   <li>The greeting endpoint accepts valid JWTs with {@code ROLE_GREETING_ADMIN} with HTTP 202.</li>
 * </ul>
 *
 * <p>{@link GreetingEventPublisher} is mocked to prevent Kafka publish attempts during tests.
 * {@code @EmbeddedKafka} satisfies Kafka auto-configuration requirements in the full context.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = "${egds.kafka.topic.greeting:egds.greeting.events.test}")
class SecurityLayerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GreetingEventPublisher greetingEventPublisher;

    @Test
    @DisplayName("POST /api/v1/auth/token with valid credentials returns HTTP 200 and a JWT")
    void tokenEndpoint_validCredentials_returnsJwt() throws Exception {
        mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tokenRequestBody("greeting.admin", "egds-admin-pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/token with wrong password returns HTTP 401")
    void tokenEndpoint_wrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tokenRequestBody("greeting.admin", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/greeting without Authorization header returns HTTP 401")
    void greetingEndpoint_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/greeting"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/greeting with malformed token returns HTTP 401")
    void greetingEndpoint_malformedToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/greeting")
                        .header("Authorization", "Bearer this.is.not.a.valid.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/greeting with valid ROLE_GREETING_ADMIN JWT returns HTTP 202")
    void greetingEndpoint_validAdminToken_returns202() throws Exception {
        String token = obtainToken("greeting.admin", "egds-admin-pass");

        mockMvc.perform(get("/api/v1/greeting")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.correlationId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    // --- helpers ---

    private String tokenRequestBody(String username, String password) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "username", username,
                "password", password
        ));
    }

    private String obtainToken(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tokenRequestBody(username, password)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(body).get("token").asText();
        assertThat(token).isNotBlank();
        return token;
    }
}
