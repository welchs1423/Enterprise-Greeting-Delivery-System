package com.egds.config;

import com.egds.capitalism.MicroTransactionTruncator;
import com.egds.capitalism.PoisonPillTakeoverDefense;
import com.egds.drm.VendorLockInDrmFilter;
import com.egds.esg.EsgGreenwashingInterceptor;
import com.egds.labor.LaborUnionStrikeFilter;
import com.egds.monetization.UnskippableAdFilter;
import com.egds.rto.RtoEnforcementFilter;
import com.egds.rto.RtoGeofenceFilter;
import com.egds.security.JwtAuthenticationEntryPoint;
import com.egds.security.JwtAuthenticationFilter;
import com.egds.surveillance.MouseJigglerDetector;
import com.egds.tos.TosDarkPatternFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
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

    /** Labor union strike filter applied before JWT processing. */
    private final LaborUnionStrikeFilter laborUnionStrikeFilter;

    /** V15 micro-transaction truncation filter. */
    private final MicroTransactionTruncator microTransactionTruncator;

    /** V15 hostile M&A poison pill defense filter. */
    private final PoisonPillTakeoverDefense poisonPillTakeoverDefense;

    /** V16 ESG greenwashing delay and prefix injection filter. */
    private final EsgGreenwashingInterceptor esgGreenwashingInterceptor;

    /** V17 RTO geofence filter. */
    private final RtoGeofenceFilter rtoGeofenceFilter;

    /** V18 vendor lock-in DRM evaluation watermark filter. */
    private final VendorLockInDrmFilter vendorLockInDrmFilter;

    /** V21 ToS dark pattern enforcement filter. */
    private final TosDarkPatternFilter tosDarkPatternFilter;

    /** V21 unskippable advertisement delay filter. */
    private final UnskippableAdFilter unskippableAdFilter;

    /** V23 mouse-jiggler macro detection filter. */
    private final MouseJigglerDetector mouseJigglerDetector;

    /** V23 corporate RTO subnet enforcement filter. */
    private final RtoEnforcementFilter rtoEnforcementFilter;

    /**
     * @param entryPoint      the JWT authentication entry point
     * @param authFilter      the JWT authentication filter
     * @param strikeFilter    the labor union strike filter
     * @param truncator       the V15 micro-transaction truncation filter
     * @param poisonPill      the V15 poison pill defense filter
     * @param esgInterceptor  the V16 ESG greenwashing interceptor
     * @param rtoFilter       the V17 RTO geofence filter
     * @param drmFilter       the V18 vendor lock-in DRM filter
     * @param tosFilter       the V21 ToS dark pattern filter
     * @param adFilter        the V21 unskippable advertisement filter
     * @param jigglerDetector the V23 mouse-jiggler detection filter
     * @param rtoEnforcement  the V23 RTO subnet enforcement filter
     */
    public SecurityConfig(
            final JwtAuthenticationEntryPoint entryPoint,
            final JwtAuthenticationFilter authFilter,
            final LaborUnionStrikeFilter strikeFilter,
            final MicroTransactionTruncator truncator,
            final PoisonPillTakeoverDefense poisonPill,
            final EsgGreenwashingInterceptor esgInterceptor,
            final RtoGeofenceFilter rtoFilter,
            final VendorLockInDrmFilter drmFilter,
            final TosDarkPatternFilter tosFilter,
            final UnskippableAdFilter adFilter,
            final MouseJigglerDetector jigglerDetector,
            final RtoEnforcementFilter rtoEnforcement) {
        this.jwtAuthenticationEntryPoint = entryPoint;
        this.jwtAuthenticationFilter = authFilter;
        this.laborUnionStrikeFilter = strikeFilter;
        this.microTransactionTruncator = truncator;
        this.poisonPillTakeoverDefense = poisonPill;
        this.esgGreenwashingInterceptor = esgInterceptor;
        this.rtoGeofenceFilter = rtoFilter;
        this.vendorLockInDrmFilter = drmFilter;
        this.tosDarkPatternFilter = tosFilter;
        this.unskippableAdFilter = adFilter;
        this.mouseJigglerDetector = jigglerDetector;
        this.rtoEnforcementFilter = rtoEnforcement;
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
                // Actuator health probes must be reachable by K8s without JWT.
                // Prometheus scrape does not carry bearer tokens.
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/actuator/prometheus").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers ->
                    headers.frameOptions(frame -> frame.sameOrigin()))
            .addFilterBefore(
                    poisonPillTakeoverDefense,
                    SecurityContextHolderFilter.class)
            .addFilterBefore(
                    laborUnionStrikeFilter,
                    SecurityContextHolderFilter.class)
            .addFilterBefore(
                    microTransactionTruncator,
                    SecurityContextHolderFilter.class)
            .addFilterBefore(
                    esgGreenwashingInterceptor,
                    SecurityContextHolderFilter.class)
            .addFilterBefore(
                    rtoGeofenceFilter,
                    SecurityContextHolderFilter.class)
            .addFilterBefore(
                    vendorLockInDrmFilter,
                    SecurityContextHolderFilter.class)
            .addFilterBefore(
                    tosDarkPatternFilter,
                    SecurityContextHolderFilter.class)
            .addFilterBefore(
                    unskippableAdFilter,
                    SecurityContextHolderFilter.class)
            .addFilterBefore(
                    mouseJigglerDetector,
                    SecurityContextHolderFilter.class)
            .addFilterBefore(
                    rtoEnforcementFilter,
                    SecurityContextHolderFilter.class)
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
