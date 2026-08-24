package com.example.sitiopro.shared.cache;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component("cacheRedis")
public class OptionalRedisHealthIndicator implements HealthIndicator {

    private final SitioProCacheProperties properties;
    private final ObjectProvider<RedisConnectionFactory> connectionFactory;

    public OptionalRedisHealthIndicator(SitioProCacheProperties properties,
            ObjectProvider<RedisConnectionFactory> connectionFactory) {
        this.properties = properties;
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Health health() {
        if (!properties.getRedis().isEnabled()) {
            return Health.up()
                    .withDetail("enabled", false)
                    .withDetail("cacheStatus", "DISABLED")
                    .build();
        }
        RedisConnectionFactory factory = connectionFactory.getIfAvailable();
        if (factory == null) {
            return indisponivel("CONFIGURATION");
        }
        try (RedisConnection connection = factory.getConnection()) {
            String pong = connection.ping();
            if ("PONG".equalsIgnoreCase(pong)) {
                return Health.up()
                        .withDetail("enabled", true)
                        .withDetail("cacheStatus", "AVAILABLE")
                        .build();
            }
            return indisponivel("UNEXPECTED_RESPONSE");
        } catch (RuntimeException ex) {
            return indisponivel(ex.getClass().getSimpleName());
        }
    }

    private Health indisponivel(String motivo) {
        return Health.up()
                .withDetail("enabled", true)
                .withDetail("optional", true)
                .withDetail("cacheStatus", "UNAVAILABLE")
                .withDetail("reason", motivo)
                .build();
    }
}
