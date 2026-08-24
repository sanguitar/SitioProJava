package com.example.sitiopro.shared.cache;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OptionalRedisHealthIndicatorTests {

    @Test
    void indisponibilidadeFicaVisivelSemDerrubarHealthNemExporMensagem() {
        SitioProCacheProperties properties = new SitioProCacheProperties();
        properties.getRedis().setEnabled(true);
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        when(factory.getConnection()).thenThrow(new IllegalStateException("password=segredo-que-nao-pode-vazar"));
        DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
        beans.registerSingleton("redisConnectionFactory", factory);
        OptionalRedisHealthIndicator indicator = new OptionalRedisHealthIndicator(
                properties, beans.getBeanProvider(RedisConnectionFactory.class));

        Health health = indicator.health();

        assertThat(health.getStatus().getCode()).isEqualTo("UP");
        assertThat(health.getDetails()).containsEntry("cacheStatus", "UNAVAILABLE");
        assertThat(health.getDetails().toString()).doesNotContain("segredo-que-nao-pode-vazar");
    }
}
