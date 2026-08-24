package com.example.sitiopro.tarefas.service;

import com.example.sitiopro.tarefas.dto.PaginaResponse;
import com.example.sitiopro.tarefas.dto.PrazoTarefa;
import com.example.sitiopro.tarefas.dto.TarefaDetalhe;
import com.example.sitiopro.tarefas.dto.TarefaFiltro;
import com.example.sitiopro.tarefas.dto.TarefaRequest;
import com.example.sitiopro.tarefas.dto.TarefaResumo;
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
import com.example.sitiopro.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTests {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 8, 24, 12, 0);

    @Mock
    private TarefaRepository tarefaRepository;
    @Mock
    private TarefaRecorrenciaRepository recorrenciaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private HistoricoOperacionalService historicoService;

    private TarefaService service;
    private Usuario admin;
    private Usuario operador;

    @BeforeEach
    void preparar() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-24T12:00:00Z"), ZoneOffset.UTC);
        service = new TarefaService(tarefaRepository, recorrenciaRepository, usuarioRepository,
                historicoService, clock);
        admin = usuario(1L, "Administrador", "admin", PerfilUsuario.ADMIN);
        operador = usuario(2L, "Operador", "operador", PerfilUsuario.OPERADOR);
        lenient().when(historicoService.listarTarefa(any())).thenReturn(List.of());
    }

    @Test
    void criaTarefaManualRecorrenteComCriadorPersistido() {
        TarefaRequest request = request("Verificar caixa d'água");
        request.setResponsavelId(operador.getId());
        request.setDataVencimento(AGORA.plusDays(1));
        request.setRecorrencia(TipoRecorrencia.SEMANAL);
        when(usuarioRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(usuarioRepository.findById(operador.getId())).thenReturn(Optional.of(operador));
        when(tarefaRepository.save(any())).thenAnswer(invocation -> tarefaComId(invocation.getArgument(0), 10L));
        when(recorrenciaRepository.save(any())).thenAnswer(invocation -> recorrenciaComId(invocation.getArgument(0), 20L));

        TarefaDetalhe detalhe = service.criar(request, new UsuarioAtor(admin.getId(), "admin", true));

        assertThat(detalhe.id()).isEqualTo(10L);
        assertThat(detalhe.status()).isEqualTo(StatusTarefa.PENDENTE);
        assertThat(detalhe.origem()).isEqualTo(OrigemTarefa.MANUAL);
        assertThat(detalhe.criadoPorId()).isEqualTo(admin.getId());
        assertThat(detalhe.responsavelId()).isEqualTo(operador.getId());
        assertThat(detalhe.recorrencia()).isEqualTo(TipoRecorrencia.SEMANAL);
        assertThat(detalhe.proximaOcorrenciaEm()).isEqualTo(AGORA.plusDays(8));
        verify(historicoService).registrarTarefa(any(), eq(com.example.sitiopro.tarefas.entity.TipoEventoOperacional.TAREFA_CRIADA),
                eq(admin), eq("admin"), any());
    }

    @Test
    void editaSomenteCamposPermitidosDaTarefa() {
        Tarefa tarefa = tarefa(30L, operador, operador);
        when(tarefaRepository.buscarParaAtualizacao(30L)).thenReturn(Optional.of(tarefa));
        when(recorrenciaRepository.buscarPorModeloParaAtualizacao(30L)).thenReturn(Optional.empty());
        when(usuarioRepository.findById(operador.getId())).thenReturn(Optional.of(operador));
        TarefaRequest request = request("Limpar galinheiro");
        request.setDescricao("Trocar a forração");
        request.setPrioridade(PrioridadeTarefa.ALTA);

        TarefaDetalhe detalhe = service.atualizar(30L, request,
                new UsuarioAtor(operador.getId(), "operador", false));

        assertThat(detalhe.titulo()).isEqualTo("Limpar galinheiro");
        assertThat(detalhe.prioridade()).isEqualTo(PrioridadeTarefa.ALTA);
        assertThat(detalhe.criadoPorId()).isEqualTo(operador.getId());
        assertThat(detalhe.origem()).isEqualTo(OrigemTarefa.MANUAL);
        assertThat(detalhe.status()).isEqualTo(StatusTarefa.PENDENTE);
    }

    @Test
    void iniciaEConcluiDeFormaIdempotente() {
        Tarefa tarefa = tarefa(40L, operador, operador);
        when(tarefaRepository.buscarParaAtualizacao(40L)).thenReturn(Optional.of(tarefa));
        when(usuarioRepository.findById(operador.getId())).thenReturn(Optional.of(operador));
        when(recorrenciaRepository.findByTarefaModeloId(40L)).thenReturn(Optional.empty());
        UsuarioAtor ator = new UsuarioAtor(operador.getId(), "operador", false);

        assertThat(service.iniciar(40L, ator).status()).isEqualTo(StatusTarefa.EM_ANDAMENTO);
        assertThat(service.iniciar(40L, ator).status()).isEqualTo(StatusTarefa.EM_ANDAMENTO);
        TarefaDetalhe concluida = service.concluir(40L, ator);
        assertThat(concluida.status()).isEqualTo(StatusTarefa.CONCLUIDA);
        assertThat(concluida.dataConclusao()).isEqualTo(AGORA);
        assertThat(concluida.ativo()).isFalse();
        assertThat(service.concluir(40L, ator).status()).isEqualTo(StatusTarefa.CONCLUIDA);
    }

    @Test
    void cancelaTarefaEDesativaSuaRecorrencia() {
        Tarefa tarefa = tarefa(50L, admin, operador);
        TarefaRecorrencia recorrencia = new TarefaRecorrencia();
        recorrencia.setTarefaModelo(tarefa);
        recorrencia.setTipo(TipoRecorrencia.MENSAL);
        recorrencia.setProximaOcorrenciaEm(AGORA.plusMonths(1));
        recorrencia.setAtiva(true);
        when(tarefaRepository.buscarParaAtualizacao(50L)).thenReturn(Optional.of(tarefa));
        when(usuarioRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(recorrenciaRepository.buscarPorModeloParaAtualizacao(50L)).thenReturn(Optional.of(recorrencia));
        when(recorrenciaRepository.findByTarefaModeloId(50L)).thenReturn(Optional.of(recorrencia));

        TarefaDetalhe detalhe = service.cancelar(50L, new UsuarioAtor(admin.getId(), "admin", true));

        assertThat(detalhe.status()).isEqualTo(StatusTarefa.CANCELADA);
        assertThat(detalhe.ativo()).isFalse();
        assertThat(recorrencia.isAtiva()).isFalse();
    }

    @Test
    void marcaTarefaVencidaNoReadModel() {
        Tarefa tarefa = tarefa(60L, operador, operador);
        tarefa.setDataVencimento(AGORA.minusMinutes(1));
        when(tarefaRepository.buscar(isNull(), isNull(), isNull(), eq(true), eq(AGORA),
                isNull(), isNull(), any())).thenReturn(new PageImpl<>(List.of(tarefa)));
        TarefaFiltro filtro = new TarefaFiltro();
        filtro.setPrazo(PrazoTarefa.VENCIDAS);

        PaginaResponse<TarefaResumo> pagina = service.listar(filtro);

        assertThat(pagina.conteudo()).singleElement().extracting(TarefaResumo::vencida).isEqualTo(true);
    }

    @Test
    void operadorNaoAlteraTarefaAtribuidaAOutroUsuario() {
        Usuario outro = usuario(3L, "Outro", "outro", PerfilUsuario.OPERADOR);
        Tarefa tarefa = tarefa(70L, outro, outro);
        when(tarefaRepository.buscarParaAtualizacao(70L)).thenReturn(Optional.of(tarefa));

        assertThatThrownBy(() -> service.iniciar(70L,
                new UsuarioAtor(operador.getId(), "operador", false)))
                .isInstanceOfSatisfying(TarefaAlertaOperacaoException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo("TAREFA_NAO_PERMITIDA");
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                });
        verify(usuarioRepository, never()).findById(operador.getId());
    }

    private TarefaRequest request(String titulo) {
        TarefaRequest request = new TarefaRequest();
        request.setTitulo(titulo);
        request.setPrioridade(PrioridadeTarefa.NORMAL);
        request.setRecorrencia(TipoRecorrencia.NENHUMA);
        return request;
    }

    private Tarefa tarefa(Long id, Usuario criador, Usuario responsavel) {
        Tarefa tarefa = new Tarefa();
        ReflectionTestUtils.setField(tarefa, "id", id);
        ReflectionTestUtils.setField(tarefa, "criadoEm", AGORA.minusDays(1));
        tarefa.setTitulo("Atividade");
        tarefa.setStatus(StatusTarefa.PENDENTE);
        tarefa.setPrioridade(PrioridadeTarefa.NORMAL);
        tarefa.setCriadoPorUsuario(criador);
        tarefa.setResponsavel(responsavel);
        tarefa.setOrigem(OrigemTarefa.MANUAL);
        tarefa.setAtivo(true);
        return tarefa;
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

    private Tarefa tarefaComId(Tarefa tarefa, Long id) {
        ReflectionTestUtils.setField(tarefa, "id", id);
        ReflectionTestUtils.setField(tarefa, "criadoEm", AGORA);
        return tarefa;
    }

    private TarefaRecorrencia recorrenciaComId(TarefaRecorrencia recorrencia, Long id) {
        ReflectionTestUtils.setField(recorrencia, "id", id);
        return recorrencia;
    }
}
