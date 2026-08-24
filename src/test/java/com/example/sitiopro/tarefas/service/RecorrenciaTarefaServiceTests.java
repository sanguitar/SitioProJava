package com.example.sitiopro.tarefas.service;

import com.example.sitiopro.tarefas.entity.OrigemTarefa;
import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;
import com.example.sitiopro.tarefas.entity.StatusTarefa;
import com.example.sitiopro.tarefas.entity.Tarefa;
import com.example.sitiopro.tarefas.entity.TarefaRecorrencia;
import com.example.sitiopro.tarefas.entity.TipoRecorrencia;
import com.example.sitiopro.tarefas.repository.TarefaRecorrenciaRepository;
import com.example.sitiopro.tarefas.repository.TarefaRepository;
import com.example.sitiopro.usuario.entity.PerfilUsuario;
import com.example.sitiopro.usuario.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecorrenciaTarefaServiceTests {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 8, 24, 12, 0);

    @Mock
    private TarefaRecorrenciaRepository recorrenciaRepository;
    @Mock
    private TarefaRepository tarefaRepository;
    @Mock
    private HistoricoOperacionalService historicoService;

    private RecorrenciaTarefaService service;
    private TarefaRecorrencia recorrencia;

    @BeforeEach
    void preparar() {
        TarefasAlertasProperties properties = new TarefasAlertasProperties();
        properties.setRecorrenciasLimitePorExecucao(10);
        Clock clock = Clock.fixed(Instant.parse("2026-08-24T12:00:00Z"), ZoneOffset.UTC);
        service = new RecorrenciaTarefaService(recorrenciaRepository, tarefaRepository,
                historicoService, properties, clock);

        Usuario usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", 1L);
        usuario.setNome("Administrador");
        usuario.setLogin("admin");
        usuario.setPerfil(PerfilUsuario.ADMIN);
        usuario.setAtivo(true);
        usuario.setSenhaHash("{noop}teste");
        Tarefa modelo = new Tarefa();
        ReflectionTestUtils.setField(modelo, "id", 2L);
        modelo.setTitulo("Verificar reservatório");
        modelo.setStatus(StatusTarefa.PENDENTE);
        modelo.setPrioridade(PrioridadeTarefa.NORMAL);
        modelo.setCriadoPorUsuario(usuario);
        modelo.setResponsavel(usuario);
        modelo.setOrigem(OrigemTarefa.MANUAL);
        modelo.setAtivo(true);
        recorrencia = new TarefaRecorrencia();
        ReflectionTestUtils.setField(recorrencia, "id", 3L);
        recorrencia.setTarefaModelo(modelo);
        recorrencia.setTipo(TipoRecorrencia.DIARIA);
        recorrencia.setProximaOcorrenciaEm(AGORA);
        recorrencia.setAtiva(true);
    }

    @Test
    void execucaoRepetidaNaoDuplicaMesmaOcorrencia() {
        when(recorrenciaRepository.buscarIdsVencidos(eq(AGORA), any())).thenReturn(List.of(3L));
        when(recorrenciaRepository.buscarParaAtualizacao(3L)).thenReturn(Optional.of(recorrencia));
        AtomicLong ids = new AtomicLong(100);
        when(tarefaRepository.save(any())).thenAnswer(invocation -> {
            Tarefa tarefa = invocation.getArgument(0);
            ReflectionTestUtils.setField(tarefa, "id", ids.getAndIncrement());
            return tarefa;
        });
        when(tarefaRepository.existsByRecorrenciaOrigemIdAndOcorrenciaProgramadaEm(3L, AGORA))
                .thenReturn(false, true);

        assertThat(service.gerarOcorrenciasVencidas()).isEqualTo(1);
        recorrencia.setProximaOcorrenciaEm(AGORA);
        assertThat(service.gerarOcorrenciasVencidas()).isZero();

        verify(tarefaRepository, times(1)).save(any());
        assertThat(recorrencia.getProximaOcorrenciaEm()).isEqualTo(AGORA.plusDays(1));
    }

    @Test
    void geraOcorrenciasAtrasadasAteAlcancarProximaData() {
        recorrencia.setProximaOcorrenciaEm(AGORA.minusDays(2));
        when(recorrenciaRepository.buscarIdsVencidos(eq(AGORA), any())).thenReturn(List.of(3L));
        when(recorrenciaRepository.buscarParaAtualizacao(3L)).thenReturn(Optional.of(recorrencia));
        when(tarefaRepository.existsByRecorrenciaOrigemIdAndOcorrenciaProgramadaEm(eq(3L), any()))
                .thenReturn(false);
        when(tarefaRepository.save(any())).thenAnswer(invocation -> {
            Tarefa tarefa = invocation.getArgument(0);
            ReflectionTestUtils.setField(tarefa, "id", 100L);
            return tarefa;
        });

        assertThat(service.gerarOcorrenciasVencidas()).isEqualTo(3);
        assertThat(recorrencia.getProximaOcorrenciaEm()).isEqualTo(AGORA.plusDays(1));
        verify(tarefaRepository, times(3)).save(any());
    }
}
