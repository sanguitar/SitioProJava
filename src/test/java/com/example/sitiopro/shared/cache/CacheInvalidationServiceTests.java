package com.example.sitiopro.shared.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(CacheInvalidationServiceTests.Config.class)
class CacheInvalidationServiceTests {

    @jakarta.annotation.Resource
    private CacheManager cacheManager;

    @jakarta.annotation.Resource
    private CacheInvalidationService invalidationService;

    @BeforeEach
    void preencher() {
        cacheManager.getCache(CacheNames.CLIMA_RESUMO).put("principal", "clima");
        cacheManager.getCache(CacheNames.INTEGRACOES_STATUS).put("painel-completo", "status");
        cacheManager.getCache(CacheNames.AGROFIT_CULTURAS).put("catalogo-completo", "culturas");
    }

    @Test
    void invalidaCachesDosDadosSincronizadosEDoEstadoOperacional() {
        invalidationService.invalidarClimaResumo();
        invalidationService.invalidarIntegracoesStatus();
        invalidationService.invalidarAgrofitCulturas();

        assertThat(cacheManager.getCache(CacheNames.CLIMA_RESUMO).get("principal")).isNull();
        assertThat(cacheManager.getCache(CacheNames.INTEGRACOES_STATUS).get("painel-completo")).isNull();
        assertThat(cacheManager.getCache(CacheNames.AGROFIT_CULTURAS).get("catalogo-completo")).isNull();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableCaching
    @Import(CacheInvalidationService.class)
    static class Config {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(
                    CacheNames.CLIMA_RESUMO,
                    CacheNames.INTEGRACOES_STATUS,
                    CacheNames.AGROFIT_CULTURAS);
        }
    }
}
