package com.egds.web;

import com.egds.cqrs.command.DeliverGreetingCommand;
import com.egds.cqrs.command.GreetingCommandHandler;
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
 * CQRS command-side REST endpoint for EGDS greeting delivery.
 *
 * <p>This controller operates exclusively on the write path. It
 * constructs a {@link DeliverGreetingCommand} from the incoming HTTP
 * request and delegates to {@link GreetingCommandHandler}, which
 * publishes the authoritative {@code GreetingRequestedEvent} to the
 * Kafka event log and triggers the legacy delivery pipeline.
 *
 * <p>The endpoint returns HTTP 202 Accepted immediately. All downstream
 * processing is asynchronous. Clients poll the query endpoint
 * {@code GET /api/v1/greeting/status/{correlationId}} to retrieve the
 * projected read model from MongoDB.
 *
 * <p>All operations require a valid JWT bearer token carrying the
 * {@code ROLE_GREETING_ADMIN} authority.
 */
@RestController
@RequestMapping("/api/v1")
public class GreetingController {

    /** CQRS command handler for greeting delivery commands. */
    private final GreetingCommandHandler commandHandler;

    /**
     * @param handler the CQRS greeting command handler
     */
    public GreetingController(final GreetingCommandHandler handler) {
        this.commandHandler = handler;
    }

    /**
     * Accepts a greeting delivery command and returns HTTP 202 Accepted.
     * Requires the {@code ROLE_GREETING_ADMIN} authority.
     *
     * <p>Processing flow:
     * <ol>
     *   <li>Resolve client IP (honouring {@code X-Forwarded-For}).</li>
     *   <li>Build a {@link DeliverGreetingCommand} and dispatch to
     *       {@link GreetingCommandHandler}.</li>
     *   <li>Return HTTP 202 with the correlation ID for async polling.
     *       </li>
     * </ol>
     *
     * @param request the HTTP request for capturing the client IP
     * @return HTTP 202 with a {@link GreetingResponse} containing the
     *         correlation ID
     */
    @GetMapping("/greeting")
    @PreAuthorize("hasRole('GREETING_ADMIN')")
    public ResponseEntity<GreetingResponse> deliverGreeting(
            final HttpServletRequest request) {
        String correlationId = UUID.randomUUID().toString();
        String requestIp = resolveClientIp(request);
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();
        String principalName = (auth != null)
                ? auth.getName() : "ANONYMOUS";

        commandHandler.handle(
                new DeliverGreetingCommand(
                        correlationId, requestIp, principalName));

        return ResponseEntity.accepted()
                .body(new GreetingResponse(correlationId));
    }

    /**
     * Resolves the real client IP address, preferring
     * {@code X-Forwarded-For} for requests routed through a proxy.
     *
     * @param request the HTTP request
     * @return the resolved IP address string
     */
    private String resolveClientIp(final HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
