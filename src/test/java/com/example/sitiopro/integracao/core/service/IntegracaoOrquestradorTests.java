package com.example.sitiopro.integracao.core.service;

import com.example.sitiopro.integracao.core.FonteIntegracao;
import com.example.sitiopro.integracao.core.IntegracaoHttpException;
import com.example.sitiopro.integracao.core.IntegracaoOperacaoException;
import com.example.sitiopro.integracao.core.IntegracaoSincronizador;
import com.example.sitiopro.integracao.core.ResultadoSincronizacao;
import com.example.sitiopro.integracao.core.dto.IntegracaoExecucaoResumo;
import com.example.sitiopro.integracao.core.entity.IntegracaoExecucao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegracaoOrquestradorTests {

    @Mock
    private IntegracaoSincronizador sincronizador;

    @Mock
    private IntegracaoExecucaoService execucaoService;

    private IntegracaoOrquestrador orquestrador;

    @BeforeEach
    void configurar() {
        when(sincronizador.fonte()).thenReturn(FonteIntegracao.OPEN_METEO);
        orquestrador = new IntegracaoOrquestrador(List.of(sincronizador), execucaoService);
    }

    @Test
    void concluiExecucaoComContadoresDoProvedor() {
        prepararDisponivel();
        IntegracaoExecucao execucao = execucao(10L);
        ResultadoSincronizacao resultado = new ResultadoSincronizacao(168, 160, 4, 4);
        IntegracaoExecucaoResumo concluida = resumo(10L, resultado);
        when(execucaoService.iniciar(org.mockito.ArgumentMatchers.eq(FonteIntegracao.OPEN_METEO), anyString()))
                .thenReturn(execucao);
        when(sincronizador.sincronizar()).thenReturn(resultado);
        when(execucaoService.concluir(10L, resultado)).thenReturn(concluida);

        IntegracaoExecucaoResumo retorno = orquestrador.sincronizar(FonteIntegracao.OPEN_METEO);

        assertThat(retorno.registrosLidos()).isEqualTo(168);
        verify(execucaoService).concluir(10L, resultado);
        verify(execucaoService, never()).falhar(org.mockito.ArgumentMatchers.anyLong(), anyString(), anyString());
    }

    @Test
    void falhaFinalizaExecucaoEPreservaFluxoLocal() {
        prepararDisponivel();
        IntegracaoExecucao execucao = execucao(11L);
        when(execucaoService.iniciar(org.mockito.ArgumentMatchers.eq(FonteIntegracao.OPEN_METEO), anyString()))
                .thenReturn(execucao);
        when(sincronizador.sincronizar()).thenThrow(new IntegracaoHttpException(
                "API_TIMEOUT", "Tempo limite.", null, IntegracaoHttpException.Tipo.TIMEOUT));
        when(execucaoService.falhar(11L, "API_TIMEOUT", "Tempo limite."))
                .thenReturn(resumo(11L, new ResultadoSincronizacao(0, 0, 0, 0)));

        assertThatThrownBy(() -> orquestrador.sincronizar(FonteIntegracao.OPEN_METEO))
                .isInstanceOf(IntegracaoOperacaoException.class)
                .hasMessageContaining("dados locais anteriores foram preservados");
        verify(execucaoService).falhar(11L, "API_TIMEOUT", "Tempo limite.");
    }

    @Test
    void naoIniciaQuandoProvedorEstaDesabilitado() {
        when(sincronizador.habilitada()).thenReturn(false);

        assertThatThrownBy(() -> orquestrador.sincronizar(FonteIntegracao.OPEN_METEO))
                .isInstanceOf(IntegracaoOperacaoException.class)
                .extracting("code")
                .isEqualTo("INTEGRACAO_DESABILITADA");
        verify(execucaoService, never()).iniciar(org.mockito.ArgumentMatchers.any(), anyString());
    }

    private void prepararDisponivel() {
        when(sincronizador.habilitada()).thenReturn(true);
        when(sincronizador.configurada()).thenReturn(true);
    }

    private IntegracaoExecucao execucao(Long id) {
        IntegracaoExecucao execucao = new IntegracaoExecucao(
                FonteIntegracao.OPEN_METEO, LocalDateTime.of(2026, 8, 21, 12, 0), "trace-123");
        ReflectionTestUtils.setField(execucao, "id", id);
        return execucao;
    }

    private IntegracaoExecucaoResumo resumo(Long id, ResultadoSincronizacao resultado) {
        return new IntegracaoExecucaoResumo(
                id,
                LocalDateTime.of(2026, 8, 21, 12, 0),
                LocalDateTime.of(2026, 8, 21, 12, 1),
                com.example.sitiopro.integracao.core.StatusExecucaoIntegracao.SUCCESS,
                resultado.lidos(), resultado.inseridos(), resultado.atualizados(), resultado.ignorados(),
                null, null, "trace-123", 60_000L);
    }
}
