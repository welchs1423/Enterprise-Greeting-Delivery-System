package com.egds.core.service;

import com.egds.config.CacheConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * High-performance greeting string assembly service backed by a cache.
 *
 * <p>Assembles discrete greeting tokens into a fully-formed greeting
 * string. The assembled result is cached per token pair to eliminate
 * redundant string concatenation across repeated delivery cycles.
 *
 * <p>In production, the backing store is Redis (activated via the "prod"
 * profile). Local and CI environments use an in-memory
 * {@code ConcurrentMapCache}.
 *
 * <p>The {@link #assembleGreeting} method body executes only on cache
 * miss; subsequent calls with the same token pair return the cached value
 * without method invocation. Cache miss events are logged at INFO level.
 */
@Service
public class GreetingCacheService {

    /** Logger for this service. */
    private static final Logger LOG =
            LoggerFactory.getLogger(GreetingCacheService.class);

    /**
     * Assembles two greeting tokens into a complete greeting string.
     * The result is stored in the
     * {@value CacheConfig#GREETING_PARTS_CACHE} cache region under the
     * composite key {@code "{part1}:{part2}"}.
     *
     * <p>Cache behavior:
     * <ul>
     *   <li>Cache miss: method body executes, result stored, INFO logged.
     *   </li>
     *   <li>Cache hit: method body bypassed, cached result returned.</li>
     * </ul>
     *
     * @param part1 the opening greeting token (e.g., {@code "Hello"})
     * @param part2 the closing greeting token (e.g., {@code "World"})
     * @return the assembled greeting string
     */
    @Cacheable(
            value = CacheConfig.GREETING_PARTS_CACHE,
            key = "#part1 + ':' + #part2",
            condition = "#part1 != null and #part2 != null",
            unless = "#result == null"
    )
    public String assembleGreeting(final String part1, final String part2) {
        LOG.info("[CACHE] MISS key={}:{} - executing greeting assembly",
                part1, part2);
        return part1 + ", " + part2 + "!";
    }

    /**
     * Forces a cache update for the specified token pair, bypassing the
     * existing cached entry. The method body is always executed; the
     * returned value overwrites the current cache entry.
     * Use during maintenance windows when the cached string must refresh.
     *
     * @param part1 the opening greeting token
     * @param part2 the closing greeting token
     * @return the newly assembled greeting string
     */
    @CachePut(
            value = CacheConfig.GREETING_PARTS_CACHE,
            key = "#part1 + ':' + #part2"
    )
    public String refreshGreeting(final String part1, final String part2) {
        LOG.info("[CACHE] PUT key={}:{} - forcing cache refresh",
                part1, part2);
        return part1 + ", " + part2 + "!";
    }

    /**
     * Evicts all entries from the greeting parts cache.
     * Intended for use during configuration changes or post-deployment
     * cache invalidation.
     */
    @CacheEvict(value = CacheConfig.GREETING_PARTS_CACHE, allEntries = true)
    public void evictAll() {
        LOG.info("[CACHE] EVICT_ALL - greeting-parts cache cleared");
    }
}
