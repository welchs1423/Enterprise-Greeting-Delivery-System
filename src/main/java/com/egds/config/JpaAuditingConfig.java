package com.egds.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * JPA Auditing configuration.
 *
 * <p>Separated from {@code @SpringBootApplication} into a dedicated
 * {@code @Configuration} class to prevent {@code @DataJpaTest} slice failures
 * caused by missing {@code AuditorAware} beans during test context loading.
 *
 * <p>The {@code AuditorAware} implementation resolves the current principal from
 * the Spring Security context, falling back to the literal {@code "SYSTEM"} when
 * no authenticated principal is available (e.g., async Kafka consumer threads).
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    /**
     * Resolves the current auditor from the Spring Security context.
     * Falls back to {@code "SYSTEM"} when no authenticated principal is present.
     *
     * @return an {@link AuditorAware} implementation backed by {@link SecurityContextHolder}
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("SYSTEM");
            }
            return Optional.of(authentication.getName());
        };
    }
}
