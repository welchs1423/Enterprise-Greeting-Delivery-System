package com.egds.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Entry point invoked by Spring Security when an unauthenticated request
 * attempts to access a protected resource.
 *
 * <p>Returns a structured JSON error body with HTTP 401 Unauthorized
 * instead of the default Spring Security redirect or empty response.
 * All EGDS endpoints except {@code /api/v1/auth/**} are protected.
 */
@Component
public class JwtAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    /** Jackson object mapper for writing the JSON error response body. */
    private final ObjectMapper objectMapper;

    /**
     * @param mapper the Jackson object mapper
     */
    public JwtAuthenticationEntryPoint(final ObjectMapper mapper) {
        this.objectMapper = mapper;
    }

    /**
     * Responds to unauthenticated access with HTTP 401 and a JSON body.
     *
     * @param request       the request that triggered the auth failure
     * @param response      the response to populate with the 401 body
     * @param authException the exception that triggered this handler
     * @throws IOException if response writing fails
     */
    @Override
    public void commence(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = Map.of(
                "status", HttpServletResponse.SC_UNAUTHORIZED,
                "error", "Unauthorized",
                "message",
                "A valid JWT bearer token is required to access"
                + " this resource.",
                "path", request.getServletPath()
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
