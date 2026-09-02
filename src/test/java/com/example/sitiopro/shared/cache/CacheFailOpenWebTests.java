package com.example.sitiopro.shared.cache;

import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalService;
import static com.example.sitiopro.administracao.configuracao.ConfiguracaoOperacionalTestFixture.servico;
import com.example.sitiopro.integracao.api.ClimaApiController;
import com.example.sitiopro.integracao.clima.openmeteo.OpenMeteoProperties;
import com.example.sitiopro.integracao.clima.repository.PrevisaoClimaticaRepository;
import com.example.sitiopro.integracao.clima.service.ClimaConsultaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(CacheFailOpenWebTests.Config.class)
class CacheFailOpenWebTests {

    @jakarta.annotation.Resource
    private ClimaApiController controller;

    @jakarta.annotation.Resource
    private PrevisaoClimaticaRepository repository;

    @jakarta.annotation.Resource
    private AtomicReference<RuntimeException> cacheFailure;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        reset(repository);
        when(repository.findFirstByFonteAndContextoAndDataHoraPrevisaoLessThanEqualOrderByDataHoraPrevisaoDesc(
                any(), anyString(), any())).thenReturn(Optional.empty());
        when(repository.findFirstByFonteAndContextoAndDataHoraPrevisaoGreaterThanOrderByDataHoraPrevisao(
                any(), anyString(), any())).thenReturn(Optional.empty());
        when(repository.findFirstByFonteAndContextoOrderByObtidoEmDesc(any(), anyString()))
                .thenReturn(Optional.empty());
    }

    @Test
    void redisIndisponivelFazFallbackSqlEEndpointContinua200() throws Exception {
        cacheFailure.set(new RedisConnectionFailureException("indisponível"));

        mockMvc.perform(get("/api/v1/clima/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponivel").value(false));

        verify(repository)
                .findFirstByFonteAndContextoAndDataHoraPrevisaoLessThanEqualOrderByDataHoraPrevisaoDesc(
                        any(), anyString(), any());
    }

    @Test
    void timeoutRedisFazFallbackSqlEEndpointContinua200() throws Exception {
        cacheFailure.set(new QueryTimeoutException("timeout"));

        mockMvc.perform(get("/api/v1/clima/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fonte").value("open-meteo"));

        verify(repository)
                .findFirstByFonteAndContextoAndDataHoraPrevisaoLessThanEqualOrderByDataHoraPrevisaoDesc(
                        any(), anyString(), any());
    }

    @Configuration(proxyBeanMethods = false)
    @EnableCaching
    @Import({ClimaApiController.class, ClimaConsultaService.class, CacheKeyFactory.class})
    static class Config implements CachingConfigurer {

        @Bean
        AtomicReference<RuntimeException> cacheFailure() {
            return new AtomicReference<>(new RedisConnectionFailureException("indisponível"));
        }

        @Bean
        CacheManager cacheManager(AtomicReference<RuntimeException> cacheFailure) {
            Cache cache = new BrokenCache(cacheFailure);
            return new CacheManager() {
                @Override
                public Cache getCache(String name) {
                    return cache;
                }

                @Override
                public Collection<String> getCacheNames() {
                    return List.of(CacheNames.CLIMA_RESUMO);
                }
            };
        }

        @Bean
        @Override
        public CacheErrorHandler errorHandler() {
            return new FailOpenCacheErrorHandler();
        }

        @Bean
        PrevisaoClimaticaRepository previsaoClimaticaRepository() {
            return mock(PrevisaoClimaticaRepository.class);
        }

        @Bean
        OpenMeteoProperties openMeteoProperties() {
            OpenMeteoProperties properties = new OpenMeteoProperties();
            properties.setContexto("principal");
            return properties;
        }

        @Bean
        ConfiguracaoOperacionalService configuracaoOperacionalService() { return servico(); }

        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-08-24T12:00:00Z"), ZoneOffset.UTC);
        }
    }

    private static final class BrokenCache implements Cache {

        private final AtomicReference<RuntimeException> failure;

        private BrokenCache(AtomicReference<RuntimeException> failure) {
            this.failure = failure;
        }

        @Override
        public String getName() {
            return CacheNames.CLIMA_RESUMO;
        }

        @Override
        public Object getNativeCache() {
            return this;
        }

        @Override
        public ValueWrapper get(Object key) {
            throw failure.get();
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            throw failure.get();
        }

        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            throw failure.get();
        }

        @Override
        public void put(Object key, Object value) {
            throw failure.get();
        }

        @Override
        public ValueWrapper putIfAbsent(Object key, Object value) {
            throw failure.get();
        }

        @Override
        public void evict(Object key) {
            throw failure.get();
        }

        @Override
        public void clear() {
            throw failure.get();
        }
    }
}
