package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.criacao.aves.config.AvesProperties;
import com.example.sitiopro.criacao.aves.entity.IncubacaoAves;
import com.example.sitiopro.tarefas.dto.TarefaAutomaticaRequest;
import com.example.sitiopro.tarefas.service.AlertaService;
import com.example.sitiopro.tarefas.service.TarefaService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IncubacaoOperacionalServiceTests {

    @Mock private TarefaService tarefaService;
    @Mock private AlertaService alertaService;

    @Test
    void geraQuatroMarcosComChavesEDatasDeterministicas() {
        AvesProperties properties = new AvesProperties();
        IncubacaoOperacionalService service = new IncubacaoOperacionalService(
                tarefaService, alertaService, properties);
        IncubacaoAves incubacao = incubacao();
        UsuarioAtor ator = new UsuarioAtor(2L, "operador", false);

        service.garantirTarefas(incubacao, ator);
        service.garantirTarefas(incubacao, ator);

        ArgumentCaptor<TarefaAutomaticaRequest> captor = ArgumentCaptor.forClass(TarefaAutomaticaRequest.class);
        verify(tarefaService, times(8)).sincronizarAutomatica(captor.capture(), eq(ator));
        assertThat(captor.getAllValues().stream().limit(4).map(TarefaAutomaticaRequest::chaveAutomacao))
                .containsExactly(
                        "CRIACAO:AVES:INCUBACAO:1:VERIFICACAO_INICIAL",
                        "CRIACAO:AVES:INCUBACAO:1:OVOSCOPIA",
                        "CRIACAO:AVES:INCUBACAO:1:PREPARACAO_ECLOSAO",
                        "CRIACAO:AVES:INCUBACAO:1:ECLOSAO_PREVISTA");
        assertThat(captor.getAllValues().get(0).dataVencimento().toLocalDate())
                .isEqualTo(LocalDate.of(2026, 9, 2));
        assertThat(captor.getAllValues().get(1).dataVencimento().toLocalDate())
                .isEqualTo(LocalDate.of(2026, 9, 8));
        assertThat(captor.getAllValues().get(2).dataVencimento().toLocalDate())
                .isEqualTo(LocalDate.of(2026, 9, 19));
        assertThat(captor.getAllValues().get(3).dataVencimento().toLocalDate())
                .isEqualTo(LocalDate.of(2026, 9, 22));
    }

    @Test
    void permiteDesabilitarTarefasAutomaticas() {
        AvesProperties properties = new AvesProperties();
        properties.setTarefasAutomaticasEnabled(false);
        IncubacaoOperacionalService service = new IncubacaoOperacionalService(
                tarefaService, alertaService, properties);

        service.garantirTarefas(incubacao(), new UsuarioAtor(2L, "operador", false));

        verify(tarefaService, never()).sincronizarAutomatica(any(), any());
    }

    private IncubacaoAves incubacao() {
        IncubacaoAves incubacao = new IncubacaoAves();
        ReflectionTestUtils.setField(incubacao, "id", 1L);
        incubacao.setCodigo("INC-2026-0001");
        incubacao.setDataInicio(LocalDate.of(2026, 9, 1));
        incubacao.setDataPrevistaEclosao(LocalDate.of(2026, 9, 22));
        return incubacao;
    }
}
