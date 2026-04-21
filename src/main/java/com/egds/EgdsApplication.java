package com.egds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Bootstrap entry point for the Enterprise Greeting Delivery System (EGDS) v2.
 *
 * <p>Spring Boot context initialization supersedes the manual IoC wiring of v1.0.
 * Cache infrastructure is activated via {@code @EnableCaching};
 * Kafka listener container lifecycle is managed via {@code @EnableKafka}.
 *
 * <p>JPA Auditing is activated in {@link com.egds.config.JpaAuditingConfig}
 * rather than here to avoid {@code @DataJpaTest} slice failures.
 */
@SpringBootApplication
@EnableCaching
@EnableKafka
public class EgdsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EgdsApplication.class, args);
    }
}
