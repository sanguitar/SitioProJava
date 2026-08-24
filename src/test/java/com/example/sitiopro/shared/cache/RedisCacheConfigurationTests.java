package com.example.sitiopro.shared.cache;

import com.example.sitiopro.integracao.clima.dto.ClimaResumo;
import com.example.sitiopro.integracao.core.dto.IntegracaoPainelResumo;
import com.example.sitiopro.integracao.embrapa.agrofit.dto.AgrofitCulturasResumo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RedisCacheConfigurationTests {

    @Test
    void redisDesabilitadoUsaCacheManagerSemArmazenamento() {
        CacheManager manager = new DisabledCacheConfiguration().cacheManager();

        assertThat(manager).isInstanceOf(NoOpCacheManager.class);
        assertThat(manager.getCache(CacheNames.CLIMA_RESUMO)).isNotNull();
    }

    @Test
    void configuraTtlIndependentePorDominioSemSleep() {
        SitioProCacheProperties properties = new SitioProCacheProperties();
        properties.setClimaResumoTtl(Duration.ofMinutes(7));
        properties.setIntegracoesStatusTtl(Duration.ofSeconds(45));
        properties.setAgrofitCulturasTtl(Duration.ofHours(6));
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig();

        Map<String, RedisCacheConfiguration> configs = new RedisBackedCacheConfiguration()
                .cacheConfigurations(properties, mapper, defaults);

        assertThat(configs.get(CacheNames.CLIMA_RESUMO).getTtl()).isEqualTo(Duration.ofMinutes(7));
        assertThat(configs.get(CacheNames.INTEGRACOES_STATUS).getTtl()).isEqualTo(Duration.ofSeconds(45));
        assertThat(configs.get(CacheNames.AGROFIT_CULTURAS).getTtl()).isEqualTo(Duration.ofHours(6));
    }

    @Test
    void serializaEDesserializaResumoClimaticoComoJsonTipado() {
        SitioProCacheProperties properties = new SitioProCacheProperties();
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Map<String, RedisCacheConfiguration> configs = new RedisBackedCacheConfiguration()
                .cacheConfigurations(properties, mapper, RedisCacheConfiguration.defaultCacheConfig());
        RedisCacheConfiguration clima = configs.get(CacheNames.CLIMA_RESUMO);

        java.nio.ByteBuffer serialized = clima.getValueSerializationPair()
                .write(ClimaResumo.naoSincronizado());
        Object restored = clima.getValueSerializationPair().read(serialized.asReadOnlyBuffer());
        byte[] bytes = new byte[serialized.remaining()];
        serialized.asReadOnlyBuffer().get(bytes);

        assertThat(new String(bytes, java.nio.charset.StandardCharsets.UTF_8)).startsWith("{");
        assertThat(restored).isEqualTo(ClimaResumo.naoSincronizado());
    }

    @Test
    void contratosCacheadosSaoDtosSemAnotacaoJpa() {
        assertThat(ClimaResumo.class.isAnnotationPresent(jakarta.persistence.Entity.class)).isFalse();
        assertThat(IntegracaoPainelResumo.class.isAnnotationPresent(jakarta.persistence.Entity.class)).isFalse();
        assertThat(AgrofitCulturasResumo.class.isAnnotationPresent(jakarta.persistence.Entity.class)).isFalse();
    }
}
