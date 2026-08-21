package com.example.sitiopro.integracao.clima.service;

import com.example.sitiopro.integracao.clima.entity.PrevisaoClimatica;
import com.example.sitiopro.integracao.clima.openmeteo.OpenMeteoProperties;
import com.example.sitiopro.integracao.clima.openmeteo.OpenMeteoResponse;
import com.example.sitiopro.integracao.clima.repository.PrevisaoClimaticaRepository;
import com.example.sitiopro.integracao.core.ResultadoSincronizacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenMeteoPersistenceServiceTests {

    @Mock
    private PrevisaoClimaticaRepository repository;

    private OpenMeteoPersistenceService service;

    @BeforeEach
    void configurar() {
        OpenMeteoProperties properties = new OpenMeteoProperties();
        properties.setContexto("principal");
        properties.setTimezone("UTC");
        Clock clock = Clock.fixed(Instant.parse("2026-08-21T12:00:00Z"), ZoneOffset.UTC);
        service = new OpenMeteoPersistenceService(repository, properties, clock);
    }

    @Test
    void primeiraSincronizacaoInsereESegundaIgnoraValoresIguais() {
        OpenMeteoResponse response = response("28.4");
        when(repository.findByFonteAndContextoAndDataHoraPrevisaoBetweenOrderByDataHoraPrevisao(
                any(), anyString(), any(), any())).thenReturn(List.of());

        ResultadoSincronizacao primeira = service.persistir(response);

        ArgumentCaptor<List<PrevisaoClimatica>> captor = captorLista();
        verify(repository).saveAll(captor.capture());
        PrevisaoClimatica persistida = captor.getValue().getFirst();
        assertThat(primeira).isEqualTo(new ResultadoSincronizacao(1, 1, 0, 0));

        when(repository.findByFonteAndContextoAndDataHoraPrevisaoBetweenOrderByDataHoraPrevisao(
                any(), anyString(), any(), any())).thenReturn(List.of(persistida));
        ResultadoSincronizacao segunda = service.persistir(response);

        assertThat(segunda).isEqualTo(new ResultadoSincronizacao(1, 0, 0, 1));
    }

    @Test
    void sincronizacaoAtualizaSomentePrevisaoAlterada() {
        PrevisaoClimatica existente = new PrevisaoClimatica(
                com.example.sitiopro.integracao.core.FonteIntegracao.OPEN_METEO,
                "principal", "UTC", LocalDateTime.of(2026, 8, 21, 12, 0));
        existente.atualizar(new BigDecimal("27.0"), 78, new BigDecimal("1.2"), 60,
                new BigDecimal("8"), new BigDecimal("14"), new BigDecimal("0.12"),
                new BigDecimal("0.31"), 61, LocalDateTime.of(2026, 8, 21, 9, 0), "UTC");
        when(repository.findByFonteAndContextoAndDataHoraPrevisaoBetweenOrderByDataHoraPrevisao(
                any(), anyString(), any(), any())).thenReturn(List.of(existente));

        ResultadoSincronizacao resultado = service.persistir(response("28.4"));

        assertThat(resultado).isEqualTo(new ResultadoSincronizacao(1, 0, 1, 0));
        assertThat(existente.getTemperatura()).isEqualByComparingTo("28.4");
        verify(repository).saveAll(any());
    }

    @Test
    void valoresIguaisMantemContadorIgnoradoEAtualizamObtencao() {
        PrevisaoClimatica existente = new PrevisaoClimatica(
                com.example.sitiopro.integracao.core.FonteIntegracao.OPEN_METEO,
                "principal", "UTC", LocalDateTime.of(2026, 8, 21, 12, 0));
        existente.atualizar(new BigDecimal("28.4"), 78, new BigDecimal("1.2"), 60,
                new BigDecimal("8"), new BigDecimal("14"), new BigDecimal("0.12"),
                new BigDecimal("0.31"), 61, LocalDateTime.of(2026, 8, 21, 9, 0), "UTC");
        when(repository.findByFonteAndContextoAndDataHoraPrevisaoBetweenOrderByDataHoraPrevisao(
                any(), anyString(), any(), any())).thenReturn(List.of(existente));

        service.persistir(response("28.4"));

        verify(repository).saveAll(any());
        assertThat(existente.getObtidoEm()).isEqualTo(LocalDateTime.of(2026, 8, 21, 12, 0));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ArgumentCaptor<List<PrevisaoClimatica>> captorLista() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(List.class);
    }

    private OpenMeteoResponse response(String temperatura) {
        LocalDateTime data = LocalDateTime.of(2026, 8, 21, 12, 0);
        return new OpenMeteoResponse("UTC", new OpenMeteoResponse.Hourly(
                List.of(data),
                List.of(new BigDecimal(temperatura)),
                List.of(78),
                List.of(new BigDecimal("1.2")),
                List.of(60),
                List.of(new BigDecimal("8")),
                List.of(new BigDecimal("14")),
                List.of(new BigDecimal("0.12")),
                List.of(new BigDecimal("0.31")),
                List.of(61)));
    }
}
