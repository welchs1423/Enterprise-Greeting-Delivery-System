package com.egds.config;

import com.egds.surveillance.ProductivityScoreInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC configuration for the V23 employee surveillance layer.
 *
 * <p>Registers {@link ProductivityScoreInterceptor} on all
 * {@code /api/**} paths, excluding authentication and actuator
 * management endpoints.
 */
@Configuration
public class SurveillanceWebMvcConfig implements WebMvcConfigurer {

    /** The productivity surveillance interceptor bean. */
    private final ProductivityScoreInterceptor productivityScoreInterceptor;

    /**
     * @param interceptor the productivity score interceptor bean
     */
    public SurveillanceWebMvcConfig(
            final ProductivityScoreInterceptor interceptor) {
        this.productivityScoreInterceptor = interceptor;
    }

    /**
     * Registers the productivity score interceptor on all API paths,
     * excluding authentication and actuator endpoints.
     *
     * @param registry the Spring MVC interceptor registry
     */
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(productivityScoreInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/auth/**",
                        "/actuator/**");
    }
}
