package com.egds.config;

import com.egds.security.JwtAuthenticationEntryPoint;
import com.egds.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the EGDS platform.
 *
 * <p>Enforces stateless JWT-based authentication on all endpoints except
 * the token issuance path ({@code /api/v1/auth/**}). Session management
 * is STATELESS; no server-side session state is created or consulted.
 *
 * <p>Method-level security is enabled via {@code @EnableMethodSecurity},
 * allowing {@code @PreAuthorize} on individual controller methods.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /** JWT entry point for handling authentication errors. */
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /** JWT filter applied before the username/password filter. */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * @param entryPoint the JWT authentication entry point
     * @param authFilter the JWT authentication filter
     */
    public SecurityConfig(
            final JwtAuthenticationEntryPoint entryPoint,
            final JwtAuthenticationFilter authFilter) {
        this.jwtAuthenticationEntryPoint = entryPoint;
        this.jwtAuthenticationFilter = authFilter;
    }

    /**
     * Defines the primary security filter chain.
     * Requests to {@code /api/v1/auth/**} and the H2 console are
     * permitted without authentication. All other requests require
     * a valid JWT bearer token.
     *
     * @param http the {@link HttpSecurity} builder
     * @return the constructed {@link SecurityFilterChain}
     * @throws Exception if filter chain construction fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            final HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(ex ->
                    ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers ->
                    headers.frameOptions(frame -> frame.sameOrigin()))
            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Exposes the {@link AuthenticationManager} as a Spring bean for use
     * in the token issuance endpoint.
     *
     * @param config the auto-configured {@link AuthenticationConfiguration}
     * @return the resolved {@link AuthenticationManager}
     * @throws Exception if manager resolution fails
     */
    @Bean
    public AuthenticationManager authenticationManager(
            final AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
