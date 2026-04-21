package com.egds.core.service;

import com.egds.config.CacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.test.context.EmbeddedKafka;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link GreetingCacheService} verifying cache miss and cache hit behavior.
 *
 * <p>{@code @EmbeddedKafka} is required because the full application context includes
 * Kafka auto-configuration; the embedded broker prevents connection failures on startup.
 * The cache backing store is {@code ConcurrentMapCacheManager} (spring.cache.type=simple)
 * as configured in the test application.properties.
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = "${egds.kafka.topic.greeting:egds.greeting.events.test}")
class GreetingCacheServiceTest {

    @Autowired
    private GreetingCacheService greetingCacheService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void evictCacheBeforeEachTest() {
        greetingCacheService.evictAll();
    }

    @Test
    @DisplayName("assembleGreeting returns the correctly formatted greeting string")
    void assembleGreeting_returnsCorrectString() {
        String result = greetingCacheService.assembleGreeting("Hello", "World");

        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("assembleGreeting stores the result in the cache on first call (cache miss)")
    void assembleGreeting_populatesCacheOnMiss() {
        greetingCacheService.assembleGreeting("Hello", "World");

        Cache cache = cacheManager.getCache(CacheConfig.GREETING_PARTS_CACHE);
        assertThat(cache).isNotNull();
        Cache.ValueWrapper cached = cache.get("Hello:World");
        assertThat(cached).isNotNull();
        assertThat(cached.get()).isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("assembleGreeting returns cached value on second call without re-executing body")
    void assembleGreeting_returnsCachedValueOnHit() {
        String firstCall = greetingCacheService.assembleGreeting("Hello", "World");
        String secondCall = greetingCacheService.assembleGreeting("Hello", "World");

        assertThat(firstCall).isEqualTo(secondCall).isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("evictAll clears the cache so that the next call triggers a cache miss")
    void evictAll_clearsCache() {
        greetingCacheService.assembleGreeting("Hello", "World");

        greetingCacheService.evictAll();

        Cache cache = cacheManager.getCache(CacheConfig.GREETING_PARTS_CACHE);
        assertThat(cache).isNotNull();
        assertThat(cache.get("Hello:World")).isNull();
    }

    @Test
    @DisplayName("refreshGreeting overwrites the cache entry with a fresh value")
    void refreshGreeting_overwritesCacheEntry() {
        greetingCacheService.assembleGreeting("Hello", "World");
        String refreshed = greetingCacheService.refreshGreeting("Hello", "World");

        Cache cache = cacheManager.getCache(CacheConfig.GREETING_PARTS_CACHE);
        assertThat(cache).isNotNull();
        assertThat(cache.get("Hello:World").get()).isEqualTo(refreshed);
    }
}
