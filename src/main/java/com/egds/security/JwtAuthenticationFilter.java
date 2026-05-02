package com.egds.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servlet filter that extracts and validates a JWT bearer token on each
 * incoming request. On successful validation, populates the
 * {@link SecurityContextHolder} with a fully authenticated
 * {@link UsernamePasswordAuthenticationToken}.
 *
 * <p>Requests without a valid token are forwarded to the next filter
 * unchanged; the authorization decision is deferred to the Spring
 * Security filter chain. Runs exactly once per request via
 * {@link OncePerRequestFilter}.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** HTTP header name carrying the bearer token. */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /** Prefix expected in the Authorization header value. */
    private static final String BEARER_PREFIX = "Bearer ";

    /** JWT token provider for validation and claims extraction. */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * @param tokenProvider the JWT token provider
     */
    public JwtAuthenticationFilter(
            final JwtTokenProvider tokenProvider) {
        this.jwtTokenProvider = tokenProvider;
    }

    /**
     * Intercepts each request, extracts the bearer token from the
     * Authorization header, validates it, and sets the security context
     * if validation succeeds.
     *
     * @param request     the incoming HTTP request
     * @param response    the outgoing HTTP response
     * @param filterChain the remaining filter chain
     * @throws ServletException if filter processing fails
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain)
            throws ServletException, IOException {
        String token = extractBearerToken(request);

        if (StringUtils.hasText(token)
                && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.extractUsername(token);
            List<SimpleGrantedAuthority> authorities =
                    jwtTokenProvider.extractRoles(token)
                            .stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username, null, authorities);
            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request));
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the raw JWT string from the Authorization header.
     * Returns {@code null} if the header is absent or has no Bearer token.
     *
     * @param request the incoming HTTP request
     * @return the raw JWT string, or {@code null} if not present
     */
    private String extractBearerToken(final HttpServletRequest request) {
        String headerValue = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(headerValue)
                && headerValue.startsWith(BEARER_PREFIX)) {
            return headerValue.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
