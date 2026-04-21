package com.egds.grpc;

import com.egds.grpc.proto.DeliveryStatus;
import com.egds.grpc.proto.GreetingPriority;
import com.egds.grpc.proto.GreetingRequest;
import com.egds.grpc.proto.GreetingResponse;
import com.egds.grpc.proto.GreetingServiceGrpc;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * gRPC client component for invoking the EGDS {@code GreetingService} RPC.
 *
 * <p>Consumes the blocking stub injected by {@code grpc-spring-boot-starter} via
 * {@code @GrpcClient}. The target address is resolved from the application property:
 * {@code grpc.client.egds-greeting-service.address}.
 *
 * <p>This component is intended for intra-cluster service-to-service calls where a
 * caller microservice needs to request a greeting delivery over the binary gRPC transport
 * rather than over the HTTP REST or Kafka async path.
 */
@Component
public class GreetingGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(GreetingGrpcClient.class);

    @GrpcClient("egds-greeting-service")
    private GreetingServiceGrpc.GreetingServiceBlockingStub blockingStub;

    /**
     * Invokes the {@code DeliverGreeting} unary RPC on the configured EGDS server.
     *
     * @param principalName the authenticated subject issuing the request
     * @param requestIp     the originating IP address; used for audit trail
     * @return the {@link GreetingResponse} returned by the server
     * @throws StatusRuntimeException if the RPC fails or the server returns a non-OK status
     */
    public GreetingResponse deliverGreeting(String principalName, String requestIp) {
        String correlationId = UUID.randomUUID().toString();
        log.info("gRPC client DeliverGreeting: correlationId={} principal={}", correlationId, principalName);

        GreetingRequest request = GreetingRequest.newBuilder()
                .setCorrelationId(correlationId)
                .setPrincipalName(principalName)
                .setRequestIp(requestIp)
                .setIssuedAtEpochMs(Instant.now().toEpochMilli())
                .setPriority(GreetingPriority.PRIORITY_NORMAL)
                .build();

        GreetingResponse response = blockingStub.deliverGreeting(request);

        log.info("gRPC client DeliverGreeting received: pipelineCorrelationId={} status={}",
                response.getCorrelationId(), response.getStatus());

        if (response.getStatus() != DeliveryStatus.STATUS_DELIVERED) {
            log.warn("gRPC DeliverGreeting returned non-delivered status: correlationId={} status={}",
                    response.getCorrelationId(), response.getStatus());
        }
        return response;
    }
}
