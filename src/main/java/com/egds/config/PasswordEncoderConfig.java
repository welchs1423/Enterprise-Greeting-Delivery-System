package com.egds.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password encoding configuration.
 * Extracted into a dedicated {@code @Configuration} class to break the
 * circular dependency that would arise if {@link SecurityConfig} defined
 * the {@link PasswordEncoder} bean while also depending on
 * {@link com.egds.security.GreetingUserDetailsService}, which itself
 * requires the {@link PasswordEncoder}.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Provides a BCrypt-based password encoder bean.
     *
     * @return a {@link BCryptPasswordEncoder} instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
