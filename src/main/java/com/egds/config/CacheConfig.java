package com.egds.config;

/**
 * Shared cache name constants for the EGDS caching layer.
 *
 * <p>The backing store is determined by the active Spring profile:
 * <ul>
 *   <li>Local / CI: {@code ConcurrentMapCacheManager}
 *       (spring.cache.type=simple)</li>
 *   <li>Production: Redis
 *       (spring.cache.type=redis, activated via "prod" profile)</li>
 * </ul>
 *
 * Cache manager auto-configuration is delegated to Spring Boot based on
 * the {@code spring.cache.type} property; no explicit
 * {@code CacheManager} bean is defined here.
 */
public final class CacheConfig {

    /** Cache region for assembled greeting string fragments. */
    public static final String GREETING_PARTS_CACHE = "greeting-parts";

    private CacheConfig() {
        throw new UnsupportedOperationException("Utility class");
    }
}
