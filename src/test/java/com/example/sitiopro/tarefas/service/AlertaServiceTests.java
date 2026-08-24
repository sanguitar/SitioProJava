package com.example.sitiopro.tarefas.service;

import com.example.sitiopro.tarefas.dto.CondicaoAlerta;
import com.example.sitiopro.tarefas.dto.TarefaDetalhe;
import com.example.sitiopro.tarefas.entity.Alerta;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.SeveridadeAlerta;
import com.example.sitiopro.tarefas.entity.StatusAlerta;
import com.example.sitiopro.tarefas.entity.StatusTarefa;
import com.example.sitiopro.tarefas.entity.Tarefa;
import com.example.sitiopro.tarefas.entity.TipoAlerta;
import com.example.sitiopro.tarefas.repository.AlertaRepository;
import com.example.sitiopro.usuario.entity.PerfilUsuario;
import com.example.sitiopro.usuario.entity.Usuario;
import com.example.sitiopro.usuario.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTests {

    @Mock
    private AlertaRepository alertaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private TarefaService tarefaService;
    @Mock
    private HistoricoOperacionalService historicoService;

    private AlertaService service;
    private Usuario admin;

    @BeforeEach
    void preparar() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-24T12:00:00Z"), ZoneOffset.UTC);
        service = new AlertaService(alertaRepository, usuarioRepository, tarefaService,
                historicoService, new ObjectMapper().findAndRegisterModules(), clock);
        admin = usuario(1L, "Administrador", "admin", PerfilUsuario.ADMIN);
        lenient().when(historicoService.listarAlerta(any())).thenReturn(List.of());
    }

    @Test
    void deduplicaAtualizaResolveECriaNovaOcorrenciaQuandoCondicaoReaparece() {
        List<Alerta> persistidos = new ArrayList<>();
        AtomicLong sequencia = new AtomicLong(10);
        when(alertaRepository.save(any())).thenAnswer(invocation -> {
            Alerta alerta = invocation.getArgument(0);
            ReflectionTestUtils.setField(alerta, "id", sequencia.getAndIncrement());
            persistidos.add(alerta);
            return alerta;
        });
        when(alertaRepository.buscarAbertosParaAtualizacao(eq(ModuloOrigem.ESTOQUE),
                eq(TipoAlerta.ESTOQUE_ABAIXO_MINIMO), any())).thenAnswer(invocation -> persistidos.stream()
                        .filter(alerta -> alerta.getStatus() != StatusAlerta.RESOLVIDO).toList());

        CondicaoAlerta inicial = condicao("Saldo atual 2 kg", SeveridadeAlerta.ALTA, Map.of("saldo", 2));
        assertThat(service.sincronizar(ModuloOrigem.ESTOQUE,
                TipoAlerta.ESTOQUE_ABAIXO_MINIMO, List.of(inicial))).isEqualTo(1);
        assertThat(service.sincronizar(ModuloOrigem.ESTOQUE,
                TipoAlerta.ESTOQUE_ABAIXO_MINIMO, List.of(inicial))).isZero();

        CondicaoAlerta atualizada = condicao("Saldo atual 1 kg", SeveridadeAlerta.CRITICA, Map.of("saldo", 1));
        assertThat(service.sincronizar(ModuloOrigem.ESTOQUE,
                TipoAlerta.ESTOQUE_ABAIXO_MINIMO, List.of(atualizada))).isEqualTo(1);
        assertThat(persistidos).hasSize(1);
        assertThat(persistidos.getFirst().getSeveridade()).isEqualTo(SeveridadeAlerta.CRITICA);

        assertThat(service.sincronizar(ModuloOrigem.ESTOQUE,
                TipoAlerta.ESTOQUE_ABAIXO_MINIMO, List.of())).isEqualTo(1);
        assertThat(persistidos.getFirst().getStatus()).isEqualTo(StatusAlerta.RESOLVIDO);

        assertThat(service.sincronizar(ModuloOrigem.ESTOQUE,
                TipoAlerta.ESTOQUE_ABAIXO_MINIMO, List.of(inicial))).isEqualTo(1);
        assertThat(persistidos).hasSize(2);
        assertThat(persistidos.getLast().getStatus()).isEqualTo(StatusAlerta.ATIVO);
        assertThat(persistidos.getLast().getId()).isNotEqualTo(persistidos.getFirst().getId());
        verify(alertaRepository, times(5)).flush();
    }

    @Test
    void reconhecimentoEIdempotenteEPersistidoComUsuario() {
        Alerta alerta = alerta(21L);
        when(alertaRepository.buscarParaAtualizacao(21L)).thenReturn(Optional.of(alerta));
        when(usuarioRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        UsuarioAtor ator = new UsuarioAtor(admin.getId(), "admin", true);

        assertThat(service.reconhecer(21L, ator).status()).isEqualTo(StatusAlerta.RECONHECIDO);
        assertThat(service.reconhecer(21L, ator).status()).isEqualTo(StatusAlerta.RECONHECIDO);

        assertThat(alerta.getReconhecidoPor()).isEqualTo(admin);
        assertThat(alerta.getReconhecidoEm()).isNotNull();
        verify(historicoService, times(1)).registrarAlerta(eq(alerta), any(), eq(admin), eq("admin"), any());
    }

    @Test
    void transformaAlertaEmUmaUnicaTarefa() {
        Alerta alerta = alerta(31L);
        Tarefa tarefa = new Tarefa();
        ReflectionTestUtils.setField(tarefa, "id", 44L);
        TarefaDetalhe detalhe = org.mockito.Mockito.mock(TarefaDetalhe.class);
        when(detalhe.id()).thenReturn(44L);
        when(alertaRepository.buscarParaAtualizacao(31L)).thenReturn(Optional.of(alerta));
        when(usuarioRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(tarefaService.criarAPartirDoAlerta(eq(alerta), any())).thenReturn(tarefa);
        when(tarefaService.detalhar(44L)).thenReturn(detalhe);
        UsuarioAtor ator = new UsuarioAtor(admin.getId(), "admin", true);

        assertThat(service.criarTarefa(31L, ator).id()).isEqualTo(44L);
        assertThat(service.criarTarefa(31L, ator).id()).isEqualTo(44L);

        assertThat(alerta.getTarefa()).isEqualTo(tarefa);
        verify(tarefaService, times(1)).criarAPartirDoAlerta(eq(alerta), any());
    }

    @Test
    void operadorNaoResolveAlerta() {
        UsuarioAtor operador = new UsuarioAtor(2L, "operador", false);

        assertThatThrownBy(() -> service.resolver(99L, operador))
                .isInstanceOfSatisfying(TarefaAlertaOperacaoException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(ex.getCode()).isEqualTo("ALERTA_RESOLUCAO_NAO_PERMITIDA");
                });
        verify(alertaRepository, never()).buscarParaAtualizacao(99L);
    }

    private CondicaoAlerta condicao(String descricao, SeveridadeAlerta severidade, Map<String, Object> contexto) {
        return new CondicaoAlerta("ESTOQUE:ITEM:1:ABAIXO_MINIMO", "Ração abaixo do mínimo",
                descricao, severidade, "ITEM:1", contexto);
    }

    private Alerta alerta(Long id) {
        Alerta alerta = new Alerta();
        ReflectionTestUtils.setField(alerta, "id", id);
        alerta.setTitulo("Ração abaixo do mínimo");
        alerta.setDescricao("Saldo insuficiente.");
        alerta.setSeveridade(SeveridadeAlerta.ALTA);
        alerta.setStatus(StatusAlerta.ATIVO);
        alerta.setModuloOrigem(ModuloOrigem.ESTOQUE);
        alerta.setTipo(TipoAlerta.ESTOQUE_ABAIXO_MINIMO);
        alerta.setReferenciaOrigem("ITEM:1");
        alerta.setChaveDeduplicacao("ESTOQUE:ITEM:1:ABAIXO_MINIMO");
        alerta.setDetectadoEm(java.time.LocalDateTime.of(2026, 8, 24, 12, 0));
        alerta.setAtualizadoEm(java.time.LocalDateTime.of(2026, 8, 24, 12, 0));
        return alerta;
    }

    private Usuario usuario(Long id, String nome, String login, PerfilUsuario perfil) {
        Usuario usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", id);
        usuario.setNome(nome);
        usuario.setLogin(login);
        usuario.setPerfil(perfil);
        usuario.setAtivo(true);
        usuario.setSenhaHash("{noop}teste");
        return usuario;
    }
}
