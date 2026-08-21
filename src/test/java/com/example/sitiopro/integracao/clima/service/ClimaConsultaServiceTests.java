package com.example.sitiopro.integracao.clima.service;

import com.example.sitiopro.integracao.clima.dto.ClimaResumo;
import com.example.sitiopro.integracao.clima.entity.PrevisaoClimatica;
import com.example.sitiopro.integracao.clima.openmeteo.OpenMeteoProperties;
import com.example.sitiopro.integracao.clima.repository.PrevisaoClimaticaRepository;
import com.example.sitiopro.integracao.core.FonteIntegracao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClimaConsultaServiceTests {

    @Mock
    private PrevisaoClimaticaRepository repository;

    @Test
    void resumeDadosLocaisESinalizaDesatualizacao() {
        PrevisaoClimatica atual = previsao(
                LocalDateTime.of(2026, 8, 21, 12, 0),
                LocalDateTime.of(2026, 8, 21, 4, 0),
                "28.4",
                "1.5");
        PrevisaoClimatica proxima = previsao(
                LocalDateTime.of(2026, 8, 21, 13, 0),
                LocalDateTime.of(2026, 8, 21, 4, 0),
                "29.0",
                "2.0");
        when(repository.findFirstByFonteAndContextoAndDataHoraPrevisaoLessThanEqualOrderByDataHoraPrevisaoDesc(
                any(), anyString(), any())).thenReturn(Optional.of(atual));
        when(repository.findFirstByFonteAndContextoOrderByObtidoEmDesc(any(), anyString()))
                .thenReturn(Optional.of(atual));
        when(repository.findByFonteAndContextoAndDataHoraPrevisaoBetweenOrderByDataHoraPrevisao(
                any(), anyString(), any(), any())).thenReturn(List.of(atual, proxima));

        ClimaResumo resumo = service().resumo();

        assertThat(resumo.disponivel()).isTrue();
        assertThat(resumo.desatualizado()).isTrue();
        assertThat(resumo.chuvaProximas24h()).isEqualByComparingTo("3.5");
    }

    @Test
    void semDadosRetornaEstadoNaoSincronizado() {
        when(repository.findFirstByFonteAndContextoAndDataHoraPrevisaoLessThanEqualOrderByDataHoraPrevisaoDesc(
                any(), anyString(), any())).thenReturn(Optional.empty());
        when(repository.findFirstByFonteAndContextoAndDataHoraPrevisaoGreaterThanOrderByDataHoraPrevisao(
                any(), anyString(), any())).thenReturn(Optional.empty());
        when(repository.findFirstByFonteAndContextoOrderByObtidoEmDesc(any(), anyString()))
                .thenReturn(Optional.empty());

        assertThat(service().resumo().disponivel()).isFalse();
    }

    private ClimaConsultaService service() {
        OpenMeteoProperties properties = new OpenMeteoProperties();
        properties.setLatitude("-3");
        properties.setLongitude("-60");
        properties.setTimezone("UTC");
        return new ClimaConsultaService(
                repository,
                properties,
                Clock.fixed(Instant.parse("2026-08-21T12:30:00Z"), ZoneOffset.UTC));
    }

    private PrevisaoClimatica previsao(LocalDateTime data, LocalDateTime obtidoEm,
            String temperatura, String chuva) {
        PrevisaoClimatica previsao = new PrevisaoClimatica(FonteIntegracao.OPEN_METEO, "principal", "UTC", data);
        previsao.atualizar(
                new BigDecimal(temperatura), 78, new BigDecimal(chuva), 60,
                new BigDecimal("8"), new BigDecimal("14"), new BigDecimal("0.12"),
                new BigDecimal("0.31"), 61, obtidoEm, "UTC");
        return previsao;
    }
}
