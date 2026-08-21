package com.example.sitiopro.integracao.core.service;

import com.example.sitiopro.integracao.core.FonteIntegracao;
import com.example.sitiopro.integracao.core.IntegracaoOperacaoException;
import com.example.sitiopro.integracao.core.config.IntegracaoCoreProperties;
import com.example.sitiopro.integracao.core.entity.IntegracaoEstado;
import com.example.sitiopro.integracao.core.repository.IntegracaoEstadoRepository;
import com.example.sitiopro.integracao.core.repository.IntegracaoExecucaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegracaoExecucaoServiceTests {

    @Mock
    private IntegracaoEstadoRepository estadoRepository;

    @Mock
    private IntegracaoExecucaoRepository execucaoRepository;

    private IntegracaoExecucaoService service;

    @BeforeEach
    void configurar() {
        IntegracaoCoreProperties properties = new IntegracaoCoreProperties();
        properties.setRunningTimeout(Duration.ofMinutes(30));
        service = new IntegracaoExecucaoService(
                estadoRepository,
                execucaoRepository,
                properties,
                Clock.fixed(Instant.parse("2026-08-21T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void recusaSegundaSincronizacaoDaMesmaFonte() {
        IntegracaoEstado estado = new IntegracaoEstado(FonteIntegracao.OPEN_METEO);
        estado.setEmExecucao(true);
        estado.setExecucaoIniciadaEm(LocalDateTime.of(2026, 8, 21, 11, 50));
        when(estadoRepository.buscarParaAtualizacao(FonteIntegracao.OPEN_METEO)).thenReturn(Optional.of(estado));

        assertThatThrownBy(() -> service.iniciar(FonteIntegracao.OPEN_METEO, "trace-123"))
                .isInstanceOf(IntegracaoOperacaoException.class)
                .extracting("code")
                .isEqualTo("INTEGRACAO_EM_EXECUCAO");
    }

    @Test
    void recuperaLockOrfaoAposTimeoutConfigurado() {
        IntegracaoEstado estado = new IntegracaoEstado(FonteIntegracao.OPEN_METEO);
        estado.setEmExecucao(true);
        estado.setExecucaoIniciadaEm(LocalDateTime.of(2026, 8, 21, 10, 0));
        when(estadoRepository.buscarParaAtualizacao(FonteIntegracao.OPEN_METEO)).thenReturn(Optional.of(estado));
        when(execucaoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.iniciar(FonteIntegracao.OPEN_METEO, "trace-123");

        verify(execucaoRepository).save(any());
    }
}
