package com.example.sitiopro.integracao.embrapa.agrofit.service;

import com.example.sitiopro.integracao.core.ResultadoSincronizacao;
import com.example.sitiopro.integracao.embrapa.agrofit.AgrofitCulturaPayload;
import com.example.sitiopro.integracao.embrapa.agrofit.entity.AgrofitCultura;
import com.example.sitiopro.integracao.embrapa.agrofit.repository.AgrofitCulturaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgrofitPersistenceServiceTests {

    @Mock
    private AgrofitCulturaRepository repository;

    @Test
    void insereCulturasNovasEDeduplicaNomesEquivalentes() {
        when(repository.findAllByOrderByNome()).thenReturn(List.of());
        AgrofitPersistenceService service = service();

        ResultadoSincronizacao resultado = service.persistir(List.of(
                new AgrofitCulturaPayload("Café"),
                new AgrofitCulturaPayload("CAFE"),
                new AgrofitCulturaPayload("Milho")));

        assertThat(resultado).isEqualTo(new ResultadoSincronizacao(3, 2, 0, 1));
        verify(repository).saveAll(any());
    }

    @Test
    void segundaSincronizacaoIdenticaEIdempotente() {
        AgrofitCultura existente = new AgrofitCultura(
                "Café", "CAFE", LocalDateTime.of(2026, 8, 20, 12, 0));
        when(repository.findAllByOrderByNome()).thenReturn(List.of(existente));

        ResultadoSincronizacao resultado = service().persistir(List.of(new AgrofitCulturaPayload("Café")));

        assertThat(resultado).isEqualTo(new ResultadoSincronizacao(1, 0, 0, 1));
        verify(repository, never()).saveAll(any());
    }

    private AgrofitPersistenceService service() {
        return new AgrofitPersistenceService(
                repository,
                Clock.fixed(Instant.parse("2026-08-21T12:00:00Z"), ZoneOffset.UTC));
    }
}
