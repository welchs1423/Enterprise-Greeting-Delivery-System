package com.egds.config;

import com.egds.metaphysics.DescartesSolipsismInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC configuration for the metaphysics layer.
 *
 * <p>Activated only when
 * {@code egds.metaphysics.solipsism.enabled=true} is present in the
 * application configuration. Registers
 * {@link DescartesSolipsismInterceptor} on all {@code /api/**} paths,
 * excluding authentication and actuator management endpoints.</p>
 *
 * <p>The conditional guard prevents this configuration from activating
 * during standard test runs, which do not supply the
 * proof-of-existence header.</p>
 */
@Configuration
@ConditionalOnProperty(
        name = "egds.metaphysics.solipsism.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class MetaphysicsWebMvcConfig implements WebMvcConfigurer {

    /** The solipsism interceptor registered on API paths. */
    private final DescartesSolipsismInterceptor solipsismInterceptor;

    /**
     * Constructs the configuration with the required interceptor.
     *
     * @param interceptor the proof-of-existence interceptor bean
     */
    public MetaphysicsWebMvcConfig(
            final DescartesSolipsismInterceptor interceptor) {
        this.solipsismInterceptor = interceptor;
    }

    /**
     * Registers the solipsism interceptor on all API paths, excluding
     * authentication and Spring Boot Actuator management endpoints.
     *
     * @param registry the Spring MVC interceptor registry
     */
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(solipsismInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/auth/**",
                        "/actuator/**");
    }
}
