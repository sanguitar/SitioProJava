package com.example.sitiopro.tarefas.service;

import com.example.sitiopro.tarefas.entity.Alerta;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.OrigemTarefa;
import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;
import com.example.sitiopro.tarefas.entity.SeveridadeAlerta;
import com.example.sitiopro.tarefas.entity.StatusAlerta;
import com.example.sitiopro.tarefas.entity.StatusTarefa;
import com.example.sitiopro.tarefas.entity.Tarefa;
import com.example.sitiopro.tarefas.entity.TipoAlerta;
import com.example.sitiopro.tarefas.repository.AlertaRepository;
import com.example.sitiopro.tarefas.repository.TarefaRepository;
import com.example.sitiopro.usuario.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumoOperacionalServiceTests {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 8, 24, 8, 0);

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private TarefaRepository.PainelContadores tarefasContadores;

    @Mock
    private AlertaRepository.PainelContadores alertasContadores;

    private ResumoOperacionalService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-24T12:00:00Z"), ZoneId.of("America/Manaus"));
        service = new ResumoOperacionalService(tarefaRepository, alertaRepository, clock);
        when(tarefaRepository.contarParaPainel(anyCollection(), any(), any(), eq(AGORA)))
                .thenReturn(tarefasContadores);
        when(alertaRepository.contarParaPainel(anyCollection())).thenReturn(alertasContadores);
    }

    @Test
    void resumoUsaConsultasAgregadasENormalizaContagensNulas() {
        when(tarefasContadores.getPendentesHoje()).thenReturn(2L);
        when(tarefasContadores.getVencidas()).thenReturn(1L);
        when(tarefasContadores.getCriticas()).thenReturn(null);
        when(alertasContadores.getAtivos()).thenReturn(3L);
        when(alertasContadores.getCriticos()).thenReturn(null);

        var resumo = service.resumo();

        assertThat(resumo.tarefasPendentesHoje()).isEqualTo(2);
        assertThat(resumo.tarefasVencidas()).isEqualTo(1);
        assertThat(resumo.tarefasCriticas()).isZero();
        assertThat(resumo.alertasAtivos()).isEqualTo(3);
        assertThat(resumo.alertasCriticos()).isZero();
        verify(tarefaRepository).contarParaPainel(anyCollection(), any(), any(), eq(AGORA));
        verify(alertaRepository).contarParaPainel(anyCollection());
    }

    @Test
    void resumoPainelMapeiaDestaquesELimitaCadaLista() {
        when(tarefasContadores.getPendentesHoje()).thenReturn(1L);
        when(tarefasContadores.getVencidas()).thenReturn(1L);
        when(tarefasContadores.getCriticas()).thenReturn(1L);
        when(tarefasContadores.getEmAndamento()).thenReturn(2L);
        when(alertasContadores.getAtivos()).thenReturn(1L);
        when(alertasContadores.getCriticos()).thenReturn(1L);
        when(alertasContadores.getAltaSeveridade()).thenReturn(0L);

        Usuario responsavel = new Usuario();
        ReflectionTestUtils.setField(responsavel, "id", 7L);
        responsavel.setNome("Operador");
        Tarefa tarefa = new Tarefa();
        ReflectionTestUtils.setField(tarefa, "id", 10L);
        tarefa.setTitulo("Verificar irrigação");
        tarefa.setStatus(StatusTarefa.PENDENTE);
        tarefa.setPrioridade(PrioridadeTarefa.CRITICA);
        tarefa.setDataVencimento(AGORA.minusHours(1));
        tarefa.setResponsavel(responsavel);
        tarefa.setOrigem(OrigemTarefa.MANUAL);
        tarefa.setModuloOrigem(ModuloOrigem.TAREFAS);

        Alerta alerta = new Alerta();
        ReflectionTestUtils.setField(alerta, "id", 20L);
        alerta.setTitulo("Integração indisponível");
        alerta.setSeveridade(SeveridadeAlerta.CRITICA);
        alerta.setStatus(StatusAlerta.ATIVO);
        alerta.setModuloOrigem(ModuloOrigem.INTEGRACOES);
        alerta.setTipo(TipoAlerta.INTEGRACAO_COM_FALHA);
        alerta.setReferenciaOrigem("INTEGRACAO:open-meteo");
        alerta.setDetectadoEm(AGORA.minusMinutes(15));
        alerta.setAtualizadoEm(AGORA.minusMinutes(10));

        when(tarefaRepository.buscarDestaquesPainel(
                anyCollection(), eq(AGORA), any(), any(), any(Pageable.class))).thenReturn(List.of(tarefa));
        when(alertaRepository.buscarDestaquesPainel(anyCollection(), any(Pageable.class)))
                .thenReturn(List.of(alerta));

        var resumo = service.resumoPainel(5);

        assertThat(resumo.tarefas().destaques()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(10L);
            assertThat(item.responsavelNome()).isEqualTo("Operador");
            assertThat(item.vencida()).isTrue();
        });
        assertThat(resumo.alertas().destaques()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(20L);
            assertThat(item.severidade()).isEqualTo(SeveridadeAlerta.CRITICA);
        });

        ArgumentCaptor<Pageable> tarefaPage = ArgumentCaptor.forClass(Pageable.class);
        verify(tarefaRepository).buscarDestaquesPainel(
                anyCollection(), eq(AGORA), any(), any(), tarefaPage.capture());
        assertThat(tarefaPage.getValue().getPageSize()).isEqualTo(5);
    }
}
