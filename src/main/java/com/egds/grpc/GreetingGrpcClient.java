package com.egds.grpc;

import com.egds.grpc.proto.DeliveryStatus;
import com.egds.grpc.proto.GreetingPriority;
import com.egds.grpc.proto.GreetingRequest;
import com.egds.grpc.proto.GreetingResponse;
import com.egds.grpc.proto.GreetingServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * gRPC client component for invoking the EGDS GreetingService RPC.
 *
 * <p>Consumes the blocking stub injected by grpc-spring-boot-starter
 * via {@code @GrpcClient}. Target address is resolved from:
 * {@code grpc.client.egds-greeting-service.address}.
 *
 * <p>Intended for intra-cluster service-to-service calls over the
 * binary gRPC transport rather than the HTTP REST or Kafka path.
 */
@Component
public class GreetingGrpcClient {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(GreetingGrpcClient.class);

    /** Blocking stub injected by grpc-spring-boot-starter. */
    @GrpcClient("egds-greeting-service")
    private GreetingServiceGrpc.GreetingServiceBlockingStub blockingStub;

    /**
     * Invokes the DeliverGreeting unary RPC on the EGDS server.
     *
     * @param principalName the authenticated subject issuing the request
     * @param requestIp     the originating IP address for the audit trail
     * @return the {@link GreetingResponse} returned by the server
     */
    public GreetingResponse deliverGreeting(
            final String principalName,
            final String requestIp) {
        String correlationId = UUID.randomUUID().toString();
        LOG.info("gRPC client DeliverGreeting:"
                + " correlationId={} principal={}",
                correlationId, principalName);

        GreetingRequest request = GreetingRequest.newBuilder()
                .setCorrelationId(correlationId)
                .setPrincipalName(principalName)
                .setRequestIp(requestIp)
                .setIssuedAtEpochMs(Instant.now().toEpochMilli())
                .setPriority(GreetingPriority.PRIORITY_NORMAL)
                .build();

        GreetingResponse response = blockingStub.deliverGreeting(request);

        LOG.info("gRPC client DeliverGreeting received:"
                + " pipelineCorrelationId={} status={}",
                response.getCorrelationId(), response.getStatus());

        if (response.getStatus() != DeliveryStatus.STATUS_DELIVERED) {
            LOG.warn("gRPC DeliverGreeting non-delivered:"
                    + " correlationId={} status={}",
                    response.getCorrelationId(), response.getStatus());
        }
        return response;
    }
}
