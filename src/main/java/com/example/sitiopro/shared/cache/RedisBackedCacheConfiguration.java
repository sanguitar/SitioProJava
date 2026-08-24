package com.example.sitiopro.shared.cache;

import com.example.sitiopro.integracao.clima.dto.ClimaResumo;
import com.example.sitiopro.integracao.core.dto.IntegracaoPainelResumo;
import com.example.sitiopro.integracao.embrapa.agrofit.dto.AgrofitCulturasResumo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "sitiopro.cache.redis", name = "enabled", havingValue = "true")
public class RedisBackedCacheConfiguration {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(SitioProCacheProperties properties) {
        SitioProCacheProperties.Redis redis = properties.getRedis();
        validar(redis);

        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration(redis.getHost(), redis.getPort());
        standalone.setUsername(redis.getUsername());
        standalone.setPassword(RedisPassword.of(redis.getPassword()));

        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(redis.getConnectTimeout())
                .build();
        ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(socketOptions)
                .build();
        LettuceClientConfiguration client = LettuceClientConfiguration.builder()
                .commandTimeout(redis.getCommandTimeout())
                .shutdownTimeout(Duration.ZERO)
                .clientOptions(clientOptions)
                .build();
        return new LettuceConnectionFactory(standalone, client);
    }

    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory connectionFactory,
            SitioProCacheProperties properties) {
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> "sitiopro:" + cacheName + "::")
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()));

        Map<String, RedisCacheConfiguration> configurations = cacheConfigurations(
                properties, cacheObjectMapper(), defaults);
        RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(configurations)
                .enableStatistics()
                .build();

        // The delegate is wrapped below, so Spring does not invoke its lifecycle directly.
        redisCacheManager.afterPropertiesSet();

        return new FailOpenCacheManager(redisCacheManager, properties.getRedis().getRetryAfter());
    }

    Map<String, RedisCacheConfiguration> cacheConfigurations(SitioProCacheProperties properties,
            ObjectMapper objectMapper, RedisCacheConfiguration defaults) {
        Map<String, RedisCacheConfiguration> configurations = new LinkedHashMap<>();
        configurations.put(CacheNames.CLIMA_RESUMO,
                typed(defaults, objectMapper, ClimaResumo.class, properties.getClimaResumoTtl()));
        configurations.put(CacheNames.INTEGRACOES_STATUS,
                typed(defaults, objectMapper, IntegracaoPainelResumo.class,
                        properties.getIntegracoesStatusTtl()));
        configurations.put(CacheNames.AGROFIT_CULTURAS,
                typed(defaults, objectMapper, AgrofitCulturasResumo.class,
                        properties.getAgrofitCulturasTtl()));
        return configurations;
    }

    private <T> RedisCacheConfiguration typed(RedisCacheConfiguration defaults, ObjectMapper objectMapper,
            Class<T> type, Duration ttl) {
        validarTtl(ttl);
        Jackson2JsonRedisSerializer<T> serializer = new Jackson2JsonRedisSerializer<>(objectMapper.copy(), type);
        return defaults.entryTtl(ttl)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }

    private ObjectMapper cacheObjectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    private void validar(SitioProCacheProperties.Redis redis) {
        if (!StringUtils.hasText(redis.getHost())
                || redis.getPort() < 1 || redis.getPort() > 65_535
                || !StringUtils.hasText(redis.getUsername())
                || !StringUtils.hasText(redis.getPassword())) {
            throw new IllegalStateException("Redis habilitado exige host, porta, usuário e senha válidos.");
        }
        validarTtl(redis.getConnectTimeout());
        validarTtl(redis.getCommandTimeout());
        validarTtl(redis.getRetryAfter());
    }

    private void validarTtl(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalStateException("Timeouts e TTLs de cache devem ser positivos.");
        }
    }
}
