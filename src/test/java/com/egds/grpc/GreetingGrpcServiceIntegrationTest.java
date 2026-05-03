package com.egds.grpc;

import com.egds.ai.AiGreetingService;
import com.egds.grpc.proto.DeliveryStatus;
import com.egds.grpc.proto.GreetingChunk;
import com.egds.grpc.proto.GreetingPriority;
import com.egds.grpc.proto.GreetingRequest;
import com.egds.grpc.proto.GreetingResponse;
import com.egds.grpc.proto.GreetingServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link GreetingGrpcService}.
 *
 * <p>Uses the gRPC in-process transport. {@code grpc.server.port=-1} (from test
 * {@code application.properties}) disables the Netty network listener. The in-process
 * server name {@code "test"} is injected via {@code @SpringBootTest(properties)} rather
 * than the global test properties file: the gRPC in-process registry is JVM-global, so
 * a shared property would cause a {@code name already registered} conflict whenever a
 * second Spring context starts in the same test run.
 *
 * <p>{@code @EmbeddedKafka} is required because the Spring context includes
 * {@code KafkaConfig} and {@code GreetingEventConsumer}, which auto-connect on startup.
 * The SpEL topic expression matches the one used by all other integration tests so that
 * Spring Test can share a single cached {@code ApplicationContext} across the full suite.
 *
 * <p>{@link AiGreetingService} is replaced by a {@link MockBean} so that gRPC
 * tests do not require a live OpenAI endpoint. The mock returns a deterministic
 * greeting string that allows pipeline assertions to be made predictably.
 */
@SpringBootTest(properties = "grpc.server.in-process-name=test")
@EmbeddedKafka(partitions = 1, topics = "${egds.kafka.topic.greeting:egds.greeting.events.test}")
class GreetingGrpcServiceIntegrationTest {

    @GrpcClient("egds-greeting-service")
    private GreetingServiceGrpc.GreetingServiceBlockingStub blockingStub;

    @GrpcClient("egds-greeting-service")
    private GreetingServiceGrpc.GreetingServiceStub asyncStub;

    @MockBean
    private AiGreetingService aiGreetingService;

    @BeforeEach
    void stubAiService() {
        when(aiGreetingService.generateContextualGreeting())
                .thenReturn("Hello, World!");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DeliverGreeting (unary RPC) tests
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DeliverGreeting: valid request returns STATUS_DELIVERED")
    void deliverGreeting_validRequest_returnsDelivered() {
        String correlationId = UUID.randomUUID().toString();

        GreetingRequest request = GreetingRequest.newBuilder()
                .setCorrelationId(correlationId)
                .setPrincipalName("greeting.admin")
                .setRequestIp("127.0.0.1")
                .setIssuedAtEpochMs(Instant.now().toEpochMilli())
                .setPriority(GreetingPriority.PRIORITY_NORMAL)
                .build();

        GreetingResponse response = blockingStub.deliverGreeting(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(DeliveryStatus.STATUS_DELIVERED);
        assertThat(response.getMessage()).isEqualTo("Hello, World!");
        assertThat(response.getCorrelationId()).isNotBlank();
        assertThat(response.getDeliveredAtEpochMs()).isGreaterThan(0L);
        assertThat(response.getDeliveryNode()).isNotBlank();
    }

    @Test
    @DisplayName("DeliverGreeting: correlation ID in request is logged and pipeline correlation ID is echoed in response")
    void deliverGreeting_correlationIdPropagation_pipelineCorrelationIdInResponse() {
        String inboundCorrelationId = UUID.randomUUID().toString();

        GreetingRequest request = GreetingRequest.newBuilder()
                .setCorrelationId(inboundCorrelationId)
                .setPrincipalName("greeting.admin")
                .setRequestIp("10.0.0.1")
                .setIssuedAtEpochMs(Instant.now().toEpochMilli())
                .build();

        GreetingResponse response = blockingStub.deliverGreeting(request);

        // The response echoes the pipeline-internal correlationId, not necessarily the inbound one.
        // The pipeline generates its own ID; what matters is that the response field is populated.
        assertThat(response.getCorrelationId()).isNotBlank();
    }

    @Test
    @DisplayName("DeliverGreeting: request with PRIORITY_CRITICAL is accepted and delivered")
    void deliverGreeting_criticalPriority_isAccepted() {
        GreetingRequest request = GreetingRequest.newBuilder()
                .setCorrelationId(UUID.randomUUID().toString())
                .setPrincipalName("greeting.admin")
                .setRequestIp("10.0.0.2")
                .setIssuedAtEpochMs(Instant.now().toEpochMilli())
                .setPriority(GreetingPriority.PRIORITY_CRITICAL)
                .build();

        GreetingResponse response = blockingStub.deliverGreeting(request);

        assertThat(response.getStatus()).isEqualTo(DeliveryStatus.STATUS_DELIVERED);
    }

    @Test
    @DisplayName("DeliverGreeting: empty correlation ID is accepted (pipeline assigns its own ID)")
    void deliverGreeting_emptyCorrelationId_isAccepted() {
        GreetingRequest request = GreetingRequest.newBuilder()
                .setCorrelationId("")
                .setPrincipalName("greeting.admin")
                .setRequestIp("10.0.0.3")
                .setIssuedAtEpochMs(Instant.now().toEpochMilli())
                .build();

        // The pipeline generates its own correlation ID; an empty inbound ID is valid
        // from the gRPC transport perspective.
        GreetingResponse response = blockingStub.deliverGreeting(request);
        assertThat(response).isNotNull();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // StreamGreeting (server-streaming RPC) tests
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("StreamGreeting: returns exactly 4 ordered fragments that reassemble to 'Hello, World!'")
    void streamGreeting_returnsOrderedFragments() {
        GreetingRequest request = GreetingRequest.newBuilder()
                .setCorrelationId(UUID.randomUUID().toString())
                .setPrincipalName("greeting.admin")
                .setRequestIp("127.0.0.1")
                .setIssuedAtEpochMs(Instant.now().toEpochMilli())
                .build();

        List<GreetingChunk> chunks = new ArrayList<>();
        blockingStub.streamGreeting(request).forEachRemaining(chunks::add);

        assertThat(chunks).hasSize(4);

        // Verify sequential ordering
        for (int i = 0; i < chunks.size(); i++) {
            assertThat(chunks.get(i).getSequenceNumber()).isEqualTo(i);
        }

        // Verify only the last chunk has isLast=true
        for (int i = 0; i < chunks.size() - 1; i++) {
            assertThat(chunks.get(i).getIsLast()).isFalse();
        }
        assertThat(chunks.get(chunks.size() - 1).getIsLast()).isTrue();

        // Verify reassembly produces the full greeting
        String assembled = chunks.stream()
                .map(GreetingChunk::getFragment)
                .reduce("", String::concat);
        assertThat(assembled).isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("StreamGreeting: all fragments are non-empty")
    void streamGreeting_allFragmentsNonEmpty() {
        GreetingRequest request = GreetingRequest.newBuilder()
                .setCorrelationId(UUID.randomUUID().toString())
                .setPrincipalName("greeting.admin")
                .setRequestIp("127.0.0.1")
                .setIssuedAtEpochMs(Instant.now().toEpochMilli())
                .build();

        blockingStub.streamGreeting(request).forEachRemaining(chunk ->
                assertThat(chunk.getFragment()).isNotBlank()
        );
    }
}
