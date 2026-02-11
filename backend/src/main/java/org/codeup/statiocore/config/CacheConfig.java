package org.codeup.statiocore.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine for performance optimization.
 *
 * Short-term cache (30s): Frequently accessed data (dashboards, spot availability)
 * Long-term cache (10min): Static data (buildings, floors)
 *
 * Cache is evicted on data-modifying operations (check-in, checkout).
 *
 * @author TonyS-dev
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "availableSpots",    // Evicted on check-in/checkout
            "buildingStats",     // Evicted on building changes
            "floorStats",        // Evicted on floor changes
            "userDashboard",     // 30s TTL for real-time feel
            "adminDashboard"     // 30s TTL for admin monitoring
        );

        // Short-term cache: 30 seconds TTL, max 1000 entries
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .recordStats());

        return cacheManager;
    }

    @Bean
    public CacheManager longTermCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "buildings",
            "floors"
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats());

        return cacheManager;
    }
}

