package com.egds.web;

import com.egds.messaging.GreetingEvent;
import com.egds.messaging.GreetingEventPublisher;
import com.egds.web.dto.GreetingResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Primary REST endpoint for EGDS greeting delivery operations.
 *
 * <p>All operations require a valid JWT bearer token carrying the
 * {@code ROLE_GREETING_ADMIN} authority. Requests without this authority
 * receive HTTP 403 Forbidden via Spring Security's method security layer.
 *
 * <p>The delivery pipeline is invoked asynchronously through the Kafka event bus.
 * The endpoint publishes a {@link GreetingEvent} and returns HTTP 202 Accepted
 * immediately; the Kafka consumer executes the pipeline on a separate thread.
 */
@RestController
@RequestMapping("/api/v1")
public class GreetingController {

    private final GreetingEventPublisher greetingEventPublisher;

    public GreetingController(GreetingEventPublisher greetingEventPublisher) {
        this.greetingEventPublisher = greetingEventPublisher;
    }

    /**
     * Initiates an asynchronous greeting delivery cycle.
     * Requires the {@code ROLE_GREETING_ADMIN} authority; returns HTTP 403 otherwise.
     *
     * <p>Processing flow:
     * <ol>
     *   <li>Resolve client IP (honouring {@code X-Forwarded-For} for proxied requests).</li>
     *   <li>Publish a {@link GreetingEvent} to the Kafka greeting event topic.</li>
     *   <li>Return HTTP 202 Accepted with the correlation identifier for downstream tracing.</li>
     * </ol>
     *
     * @param request the HTTP request, used to capture the originating client IP
     * @return HTTP 202 Accepted with a {@link GreetingResponse} containing the correlation ID
     */
    @GetMapping("/greeting")
    @PreAuthorize("hasRole('GREETING_ADMIN')")
    public ResponseEntity<GreetingResponse> deliverGreeting(HttpServletRequest request) {
        String correlationId = UUID.randomUUID().toString();
        String requestIp = resolveClientIp(request);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = authentication != null ? authentication.getName() : "ANONYMOUS";

        greetingEventPublisher.publish(new GreetingEvent(correlationId, requestIp, principalName));

        return ResponseEntity.accepted().body(new GreetingResponse(correlationId));
    }

    /**
     * Resolves the real client IP address, preferring the {@code X-Forwarded-For} header
     * to handle requests routed through a load balancer or reverse proxy.
     *
     * @param request the HTTP request
     * @return the resolved IP address string
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
