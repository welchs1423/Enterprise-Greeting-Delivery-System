package com.egds.metaphysics;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring MVC interceptor that requires each incoming HTTP request to
 * supply a philosophical proof of the client's conscious existence.
 *
 * <p>Clients must include the {@code X-Cogito-Ergo-Sum: true} request
 * header. Requests that omit this header or supply a value other than
 * {@code "true"} are rejected via
 * {@link NonExistentClientException}, which maps to HTTP 422.</p>
 *
 * <p>This interceptor is only activated when
 * {@code egds.metaphysics.solipsism.enabled=true} is present in the
 * application configuration. Authentication and actuator paths are
 * exempt from the challenge; see
 * {@link com.egds.config.MetaphysicsWebMvcConfig}.</p>
 */
@Component
public class DescartesSolipsismInterceptor implements HandlerInterceptor {

    /** Name of the required proof-of-existence request header. */
    public static final String COGITO_HEADER = "X-Cogito-Ergo-Sum";

    /** Required value of the proof-of-existence header. */
    static final String COGITO_PROOF = "true";

    /**
     * Validates the ontological proof-of-existence header on each
     * inbound request.
     *
     * @param request  the incoming HTTP servlet request
     * @param response the outgoing HTTP servlet response
     * @param handler  the resolved handler for this request
     * @return {@code true} if the client proved existence
     * @throws NonExistentClientException if the proof header is absent
     *         or does not equal {@code "true"} (case-insensitive)
     */
    @Override
    public boolean preHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler) {
        String proof = request.getHeader(COGITO_HEADER);
        if (!COGITO_PROOF.equalsIgnoreCase(proof)) {
            throw new NonExistentClientException(
                    "Client at " + request.getRemoteAddr()
                    + " failed proof-of-existence:"
                    + " X-Cogito-Ergo-Sum != true");
        }
        return true;
    }
}
