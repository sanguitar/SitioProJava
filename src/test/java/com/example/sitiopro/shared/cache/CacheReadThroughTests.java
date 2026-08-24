package com.example.sitiopro.shared.cache;

import com.example.sitiopro.integracao.clima.dto.ClimaResumo;
import com.example.sitiopro.integracao.clima.openmeteo.OpenMeteoProperties;
import com.example.sitiopro.integracao.clima.repository.PrevisaoClimaticaRepository;
import com.example.sitiopro.integracao.clima.service.ClimaConsultaService;
import com.example.sitiopro.integracao.embrapa.agrofit.dto.AgrofitCulturasResumo;
import com.example.sitiopro.integracao.embrapa.agrofit.repository.AgrofitCulturaRepository;
import com.example.sitiopro.integracao.embrapa.agrofit.service.AgrofitConsultaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(CacheReadThroughTests.Config.class)
class CacheReadThroughTests {

    @jakarta.annotation.Resource
    private ClimaConsultaService climaConsultaService;

    @jakarta.annotation.Resource
    private AgrofitConsultaService agrofitConsultaService;

    @jakarta.annotation.Resource
    private PrevisaoClimaticaRepository previsaoRepository;

    @jakarta.annotation.Resource
    private AgrofitCulturaRepository agrofitRepository;

    @jakarta.annotation.Resource
    private CacheManager cacheManager;

    @BeforeEach
    void limpar() {
        cacheManager.getCache(CacheNames.CLIMA_RESUMO).clear();
        cacheManager.getCache(CacheNames.AGROFIT_CULTURAS).clear();
        reset(previsaoRepository, agrofitRepository);
    }

    @Test
    void cacheMissConsultaSqlEHitEvitaSegundaConsultaClimatica() {
        when(previsaoRepository
                .findFirstByFonteAndContextoAndDataHoraPrevisaoLessThanEqualOrderByDataHoraPrevisaoDesc(
                        any(), anyString(), any()))
                .thenReturn(Optional.empty());
        when(previsaoRepository
                .findFirstByFonteAndContextoAndDataHoraPrevisaoGreaterThanOrderByDataHoraPrevisao(
                        any(), anyString(), any()))
                .thenReturn(Optional.empty());
        when(previsaoRepository.findFirstByFonteAndContextoOrderByObtidoEmDesc(any(), anyString()))
                .thenReturn(Optional.empty());

        ClimaResumo primeiro = climaConsultaService.resumo();
        ClimaResumo segundo = climaConsultaService.resumo();

        assertThat(primeiro).isEqualTo(segundo);
        verify(previsaoRepository)
                .findFirstByFonteAndContextoAndDataHoraPrevisaoLessThanEqualOrderByDataHoraPrevisaoDesc(
                        any(), anyString(), any());
    }

    @Test
    void catalogoAgrofitTambemArmazenaSomenteDto() {
        when(agrofitRepository.findAllByOrderByNome()).thenReturn(List.of());

        AgrofitCulturasResumo primeiro = agrofitConsultaService.listarCulturas();
        AgrofitCulturasResumo segundo = agrofitConsultaService.listarCulturas();

        assertThat(primeiro).isEqualTo(segundo);
        assertThat(primeiro.culturas()).isEmpty();
        verify(agrofitRepository).findAllByOrderByNome();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableCaching
    @Import({ClimaConsultaService.class, AgrofitConsultaService.class, CacheKeyFactory.class})
    static class Config {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(CacheNames.CLIMA_RESUMO, CacheNames.AGROFIT_CULTURAS);
        }

        @Bean
        PrevisaoClimaticaRepository previsaoClimaticaRepository() {
            return mock(PrevisaoClimaticaRepository.class);
        }

        @Bean
        AgrofitCulturaRepository agrofitCulturaRepository() {
            return mock(AgrofitCulturaRepository.class);
        }

        @Bean
        OpenMeteoProperties openMeteoProperties() {
            OpenMeteoProperties properties = new OpenMeteoProperties();
            properties.setContexto("principal");
            properties.setLatitude("-3");
            properties.setLongitude("-60");
            properties.setTimezone("UTC");
            return properties;
        }

        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-08-24T12:00:00Z"), ZoneOffset.UTC);
        }
    }
}
