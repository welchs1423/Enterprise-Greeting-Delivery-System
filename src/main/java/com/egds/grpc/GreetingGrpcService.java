package com.egds.grpc;

import com.egds.core.dto.MessageDeliveryResult;
import com.egds.core.pipeline.MessageDeliveryPipeline;
import com.egds.grpc.proto.DeliveryStatus;
import com.egds.grpc.proto.GreetingChunk;
import com.egds.grpc.proto.GreetingRequest;
import com.egds.grpc.proto.GreetingResponse;
import com.egds.grpc.proto.GreetingServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;

/**
 * gRPC server implementation for the EGDS greeting delivery service.
 *
 * <p>Implements the {@code GreetingService} RPC contract defined in {@code greeting.proto}.
 * Delegates business logic to the existing {@link MessageDeliveryPipeline} so that the
 * binary transport path shares the same validated delivery infrastructure as the Kafka path.
 *
 * <p>Registered as a gRPC service via {@code @GrpcService}; the {@code grpc-spring-boot-starter}
 * binds this to the Netty gRPC server on {@code grpc.server.port} (default: 9090).
 */
@GrpcService
public class GreetingGrpcService extends GreetingServiceGrpc.GreetingServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(GreetingGrpcService.class);

    private static final String[] GREETING_FRAGMENTS = {"Hello", ", ", "World", "!"};

    private final MessageDeliveryPipeline pipeline;

    /**
     * @param pipeline the Spring-managed pipeline facade; must not be null
     */
    public GreetingGrpcService(MessageDeliveryPipeline pipeline) {
        this.pipeline = pipeline;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Unary RPC handler. Executes the full EGDS delivery pipeline and returns a single
     * {@link GreetingResponse}. The incoming {@code correlationId} is recorded in the MDC
     * for log correlation; the pipeline generates its own internal correlationId which
     * appears in the persistence layer and is echoed back in the response.
     */
    @Override
    public void deliverGreeting(GreetingRequest request, StreamObserver<GreetingResponse> responseObserver) {
        String inboundCorrelationId = request.getCorrelationId();
        log.info("gRPC DeliverGreeting: inboundCorrelationId={} principal={} ip={}",
                inboundCorrelationId, request.getPrincipalName(), request.getRequestIp());

        try {
            MessageDeliveryResult result = pipeline.execute();

            DeliveryStatus protoStatus = result.isSuccess()
                    ? DeliveryStatus.STATUS_DELIVERED
                    : DeliveryStatus.STATUS_FAILED;

            GreetingResponse response = GreetingResponse.newBuilder()
                    .setCorrelationId(result.getCorrelationId())
                    .setMessage("Hello, World!")
                    .setStatus(protoStatus)
                    .setDeliveredAtEpochMs(Instant.now().toEpochMilli())
                    .setDeliveryNode(resolveHostname())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("gRPC DeliverGreeting complete: pipelineCorrelationId={} success={}",
                    result.getCorrelationId(), result.isSuccess());

        } catch (Exception e) {
            log.error("gRPC DeliverGreeting failed: inboundCorrelationId={}", inboundCorrelationId, e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Greeting delivery failed: " + e.getMessage())
                            .withCause(e)
                            .asRuntimeException()
            );
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Server-streaming RPC handler. Emits the greeting payload as an ordered sequence of
     * {@link GreetingChunk} messages without invoking the full pipeline. Intended for
     * consumers that require progressive rendering or partial delivery semantics.
     */
    @Override
    public void streamGreeting(GreetingRequest request, StreamObserver<GreetingChunk> responseObserver) {
        String inboundCorrelationId = request.getCorrelationId();
        log.info("gRPC StreamGreeting: inboundCorrelationId={}", inboundCorrelationId);

        try {
            for (int i = 0; i < GREETING_FRAGMENTS.length; i++) {
                GreetingChunk chunk = GreetingChunk.newBuilder()
                        .setFragment(GREETING_FRAGMENTS[i])
                        .setSequenceNumber(i)
                        .setIsLast(i == GREETING_FRAGMENTS.length - 1)
                        .build();
                responseObserver.onNext(chunk);
            }
            responseObserver.onCompleted();
            log.info("gRPC StreamGreeting complete: inboundCorrelationId={} chunks={}",
                    inboundCorrelationId, GREETING_FRAGMENTS.length);

        } catch (Exception e) {
            log.error("gRPC StreamGreeting failed: inboundCorrelationId={}", inboundCorrelationId, e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Greeting stream failed: " + e.getMessage())
                            .withCause(e)
                            .asRuntimeException()
            );
        }
    }

    private String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
