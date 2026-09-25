package com.example.sitiopro.tarefas.service;

import com.example.sitiopro.shared.observability.MdcScope;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import com.example.sitiopro.tarefas.dto.PrazoTarefa;
import com.example.sitiopro.tarefas.dto.TarefaDetalhe;
import com.example.sitiopro.tarefas.dto.TarefaFiltro;
import com.example.sitiopro.tarefas.dto.TarefaRequest;
import com.example.sitiopro.tarefas.dto.TarefaAutomaticaRequest;
import com.example.sitiopro.tarefas.dto.TarefaResumo;
import com.example.sitiopro.tarefas.dto.UsuarioOpcao;
import com.example.sitiopro.tarefas.entity.Alerta;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.OrigemTarefa;
import com.example.sitiopro.tarefas.entity.StatusTarefa;
import com.example.sitiopro.tarefas.entity.Tarefa;
import com.example.sitiopro.tarefas.entity.TarefaRecorrencia;
import com.example.sitiopro.tarefas.entity.TipoEventoOperacional;
import com.example.sitiopro.tarefas.entity.TipoRecorrencia;
import com.example.sitiopro.tarefas.repository.TarefaRecorrenciaRepository;
import com.example.sitiopro.tarefas.repository.TarefaRepository;
import com.example.sitiopro.usuario.entity.Usuario;
import com.example.sitiopro.usuario.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Service
public class TarefaService {

    private static final Logger log = LoggerFactory.getLogger(TarefaService.class);

    private final TarefaRepository tarefaRepository;
    private final TarefaRecorrenciaRepository recorrenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistoricoOperacionalService historicoService;
    private final Clock clock;

    public TarefaService(TarefaRepository tarefaRepository,
            TarefaRecorrenciaRepository recorrenciaRepository,
            UsuarioRepository usuarioRepository,
            HistoricoOperacionalService historicoService,
            Clock clock) {
        this.tarefaRepository = tarefaRepository;
        this.recorrenciaRepository = recorrenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.historicoService = historicoService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<TarefaResumo> listar(TarefaFiltro filtro) {
        LocalDateTime agora = LocalDateTime.now(clock);
        IntervaloPrazo intervalo = intervalo(filtro.getPrazo(), agora);
        Page<TarefaResumo> pagina = tarefaRepository.buscar(
                        filtro.getStatus(), filtro.getPrioridade(), filtro.getResponsavelId(),
                        filtro.getPrazo() == PrazoTarefa.VENCIDAS, agora,
                        intervalo.inicio(), intervalo.fim(),
                        PageRequest.of(filtro.getPagina(), filtro.getTamanho()))
                .map(tarefa -> resumo(tarefa, agora));
        return PaginaResponse.de(pagina);
    }

    @Transactional(readOnly = true)
    public TarefaDetalhe detalhar(Long id) {
        Tarefa tarefa = buscar(id);
        TarefaRecorrencia recorrencia = recorrenciaRepository.findByTarefaModeloId(id).orElse(null);
        return detalhe(tarefa, recorrencia, LocalDateTime.now(clock));
    }

    @Transactional(readOnly = true)
    public TarefaRequest formularioEdicao(Long id, UsuarioAtor ator) {
        Tarefa tarefa = buscar(id);
        autorizarAlteracao(tarefa, ator);
        TarefaRecorrencia recorrencia = recorrenciaRepository.findByTarefaModeloId(id).orElse(null);
        TarefaRequest request = new TarefaRequest();
        request.setTitulo(tarefa.getTitulo());
        request.setDescricao(tarefa.getDescricao());
        request.setPrioridade(tarefa.getPrioridade());
        request.setDataVencimento(tarefa.getDataVencimento());
        request.setResponsavelId(tarefa.getResponsavel() == null ? null : tarefa.getResponsavel().getId());
        request.setRecorrencia(recorrencia == null || !recorrencia.isAtiva()
                ? TipoRecorrencia.NENHUMA : recorrencia.getTipo());
        request.setIntervaloDias(recorrencia == null ? null : recorrencia.getIntervaloDias());
        return request;
    }

    @Transactional(readOnly = true)
    public java.util.List<UsuarioOpcao> listarResponsaveisAtivos() {
        return usuarioRepository.findByAtivoTrueOrderByNomeAsc().stream()
                .map(usuario -> new UsuarioOpcao(usuario.getId(), usuario.getNome(), usuario.getLogin()))
                .toList();
    }

    @Transactional
    public TarefaDetalhe criar(TarefaRequest request, UsuarioAtor ator) {
        Usuario criador = buscarAtor(ator);
        Tarefa tarefa = novaTarefa(request, criador, resolverResponsavel(request.getResponsavelId(), ator, criador));
        tarefa.setOrigem(OrigemTarefa.MANUAL);
        tarefa = tarefaRepository.save(tarefa);
        TarefaRecorrencia recorrencia = configurarRecorrencia(tarefa, request, null);
        historicoService.registrarTarefa(tarefa, TipoEventoOperacional.TAREFA_CRIADA, criador, ator.ator(),
                "Tarefa manual criada.");
        registrarLog("tarefa.created", tarefa);
        return detalhe(tarefa, recorrencia, LocalDateTime.now(clock));
    }

    @Transactional
    public TarefaDetalhe criarVinculada(TarefaRequest request, UsuarioAtor ator,
            ModuloOrigem modulo, String referencia) {
        Usuario criador = buscarAtor(ator);
        Tarefa tarefa = novaTarefa(request, criador, resolverResponsavel(request.getResponsavelId(), ator, criador));
        tarefa.setOrigem(OrigemTarefa.MANUAL);
        tarefa.setModuloOrigem(modulo);
        tarefa.setReferenciaOrigem(referencia);
        tarefa = tarefaRepository.save(tarefa);
        TarefaRecorrencia recorrencia = configurarRecorrencia(tarefa, request, null);
        historicoService.registrarTarefa(tarefa, TipoEventoOperacional.TAREFA_CRIADA, criador, ator.ator(),
                "Tarefa vinculada a " + modulo + " " + referencia + ".");
        registrarLog("tarefa.created", tarefa);
        return detalhe(tarefa, recorrencia, LocalDateTime.now(clock));
    }

    @Transactional(readOnly = true)
    public java.util.List<TarefaResumo> listarRelacionadas(ModuloOrigem modulo, String referencia) {
        LocalDateTime agora = LocalDateTime.now(clock);
        return tarefaRepository.findByModuloOrigemAndReferenciaOrigemOrderByCriadoEmDesc(modulo, referencia)
                .stream().map(tarefa -> resumo(tarefa, agora)).toList();
    }

    @Transactional
    public TarefaResumo sincronizarAutomatica(TarefaAutomaticaRequest request, UsuarioAtor ator) {
        String chave = normalizarObrigatorio(request.chaveAutomacao(), "Chave da automação");
        if (chave.length() > 160) {
            throw new TarefaAlertaOperacaoException("CHAVE_AUTOMACAO_INVALIDA",
                    "A chave da automação deve ter no máximo 160 caracteres.");
        }
        if (request.moduloOrigem() == null) {
            throw new TarefaAlertaOperacaoException("MODULO_AUTOMACAO_OBRIGATORIO",
                    "O módulo da tarefa automática é obrigatório.");
        }
        Usuario usuario = buscarAtor(ator);
        Tarefa tarefa = tarefaRepository.buscarPorChaveAutomacaoParaAtualizacao(chave).orElse(null);
        if (tarefa == null) {
            tarefa = new Tarefa();
            tarefa.setStatus(StatusTarefa.PENDENTE);
            tarefa.setResponsavel(usuario);
            tarefa.setCriadoPorUsuario(usuario);
            tarefa.setOrigem(OrigemTarefa.AUTOMATICA);
            tarefa.setModuloOrigem(request.moduloOrigem());
            tarefa.setReferenciaOrigem(normalizarObrigatorio(request.referenciaOrigem(), "Referência da automação"));
            tarefa.setChaveAutomacao(chave);
            tarefa.setAtivo(true);
            aplicarAutomacao(tarefa, request);
            tarefa = tarefaRepository.save(tarefa);
            historicoService.registrarTarefa(tarefa, TipoEventoOperacional.TAREFA_CRIADA,
                    usuario, ator.ator(), "Marco automático criado.");
            registrarLog("tarefa.created", tarefa);
        } else if (!tarefa.getStatus().finalizado()) {
            aplicarAutomacao(tarefa, request);
        }
        return resumo(tarefa, LocalDateTime.now(clock));
    }

    @Transactional
    public void concluirAutomatica(String chaveAutomacao, UsuarioAtor ator) {
        String chave = normalizarObrigatorio(chaveAutomacao, "Chave da automação");
        Tarefa tarefa = tarefaRepository.buscarPorChaveAutomacaoParaAtualizacao(chave).orElse(null);
        if (tarefa == null || tarefa.getStatus().finalizado()) {
            return;
        }
        Usuario usuario = buscarAtor(ator);
        LocalDateTime agora = LocalDateTime.now(clock);
        if (tarefa.getDataInicio() == null) {
            tarefa.setDataInicio(agora);
        }
        tarefa.setStatus(StatusTarefa.CONCLUIDA);
        tarefa.setDataConclusao(agora);
        historicoService.registrarTarefa(tarefa, TipoEventoOperacional.TAREFA_CONCLUIDA,
                usuario, ator.ator(), "Marco automático atendido pela operação de origem.");
        registrarLog("tarefa.completed", tarefa);
    }

    @Transactional
    public TarefaDetalhe atualizar(Long id, TarefaRequest request, UsuarioAtor ator) {
        Tarefa tarefa = buscarParaAtualizacao(id);
        autorizarAlteracao(tarefa, ator);
        TarefaRecorrencia recorrenciaAtual = recorrenciaRepository
                .buscarPorModeloParaAtualizacao(id).orElse(null);
        if (tarefa.getStatus().finalizado() && recorrenciaAtual == null) {
            throw conflito("TAREFA_FINALIZADA", "Tarefas concluídas ou canceladas não podem ser editadas.");
        }
        Usuario usuarioAtor = buscarAtor(ator);
        tarefa.setTitulo(normalizarObrigatorio(request.getTitulo(), "Título"));
        tarefa.setDescricao(normalizarOpcional(request.getDescricao()));
        tarefa.setPrioridade(request.getPrioridade());
        tarefa.setDataVencimento(request.getDataVencimento());
        tarefa.setResponsavel(resolverResponsavel(request.getResponsavelId(), ator, usuarioAtor));
        TarefaRecorrencia recorrencia = configurarRecorrencia(tarefa, request, recorrenciaAtual);
        historicoService.registrarTarefa(tarefa, TipoEventoOperacional.TAREFA_EDITADA, usuarioAtor, ator.ator(),
                "Dados da tarefa atualizados.");
        registrarLog("tarefa.updated", tarefa);
        return detalhe(tarefa, recorrencia, LocalDateTime.now(clock));
    }

    @Transactional
    public TarefaDetalhe iniciar(Long id, UsuarioAtor ator) {
        Tarefa tarefa = buscarParaAtualizacao(id);
        autorizarAlteracao(tarefa, ator);
        if (tarefa.getStatus() == StatusTarefa.EM_ANDAMENTO) {
            return detalheAtual(tarefa);
        }
        if (tarefa.getStatus() != StatusTarefa.PENDENTE) {
            throw conflito("TAREFA_NAO_INICIAVEL", "Apenas tarefas pendentes podem ser iniciadas.");
        }
        Usuario usuario = buscarAtor(ator);
        tarefa.setStatus(StatusTarefa.EM_ANDAMENTO);
        tarefa.setDataInicio(LocalDateTime.now(clock));
        historicoService.registrarTarefa(tarefa, TipoEventoOperacional.TAREFA_INICIADA, usuario, ator.ator(),
                "Tarefa iniciada.");
        registrarLog("tarefa.started", tarefa);
        return detalheAtual(tarefa);
    }

    @Transactional
    public TarefaDetalhe concluir(Long id, UsuarioAtor ator) {
        Tarefa tarefa = buscarParaAtualizacao(id);
        autorizarAlteracao(tarefa, ator);
        if (tarefa.getStatus() == StatusTarefa.CONCLUIDA) {
            return detalheAtual(tarefa);
        }
        if (tarefa.getStatus() == StatusTarefa.CANCELADA) {
            throw conflito("TAREFA_CANCELADA", "Uma tarefa cancelada não pode ser concluída.");
        }
        Usuario usuario = buscarAtor(ator);
        LocalDateTime agora = LocalDateTime.now(clock);
        if (tarefa.getDataInicio() == null) {
            tarefa.setDataInicio(agora);
        }
        tarefa.setStatus(StatusTarefa.CONCLUIDA);
        tarefa.setDataConclusao(agora);
        tarefa.setAtivo(false);
        historicoService.registrarTarefa(tarefa, TipoEventoOperacional.TAREFA_CONCLUIDA, usuario, ator.ator(),
                "Tarefa concluída.");
        registrarLog("tarefa.completed", tarefa);
        return detalheAtual(tarefa);
    }

    @Transactional
    public TarefaDetalhe cancelar(Long id, UsuarioAtor ator) {
        Tarefa tarefa = buscarParaAtualizacao(id);
        autorizarAlteracao(tarefa, ator);
        if (tarefa.getStatus() == StatusTarefa.CANCELADA) {
            return detalheAtual(tarefa);
        }
        if (tarefa.getStatus() == StatusTarefa.CONCLUIDA) {
            throw conflito("TAREFA_CONCLUIDA", "Uma tarefa concluída não pode ser cancelada.");
        }
        Usuario usuario = buscarAtor(ator);
        tarefa.setStatus(StatusTarefa.CANCELADA);
        tarefa.setDataConclusao(null);
        tarefa.setAtivo(false);
        recorrenciaRepository.buscarPorModeloParaAtualizacao(id).ifPresent(recorrencia -> {
            recorrencia.setAtiva(false);
            historicoService.registrarTarefa(tarefa, TipoEventoOperacional.RECORRENCIA_DESATIVADA,
                    usuario, ator.ator(), "Recorrência desativada pelo cancelamento da tarefa modelo.");
        });
        historicoService.registrarTarefa(tarefa, TipoEventoOperacional.TAREFA_CANCELADA, usuario, ator.ator(),
                "Tarefa cancelada.");
        registrarLog("tarefa.cancelled", tarefa);
        return detalheAtual(tarefa);
    }

    @Transactional
    public TarefaDetalhe desativarRecorrencia(Long id, UsuarioAtor ator) {
        Tarefa tarefa = buscarParaAtualizacao(id);
        autorizarAlteracao(tarefa, ator);
        TarefaRecorrencia recorrencia = recorrenciaRepository.buscarPorModeloParaAtualizacao(id)
                .orElseThrow(() -> new TarefaAlertaOperacaoException("RECORRENCIA_NAO_ENCONTRADA",
                        "A tarefa não possui recorrência ativa.", HttpStatus.NOT_FOUND));
        if (recorrencia.isAtiva()) {
            Usuario usuario = buscarAtor(ator);
            recorrencia.setAtiva(false);
            historicoService.registrarTarefa(tarefa, TipoEventoOperacional.RECORRENCIA_DESATIVADA,
                    usuario, ator.ator(), "Recorrência desativada.");
        }
        return detalhe(tarefa, recorrencia, LocalDateTime.now(clock));
    }

    Tarefa criarAPartirDoAlerta(Alerta alerta, UsuarioAtor ator) {
        Usuario usuario = buscarAtor(ator);
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(limitar("Tratar: " + alerta.getTitulo(), 160));
        tarefa.setDescricao(alerta.getDescricao());
        tarefa.setStatus(StatusTarefa.PENDENTE);
        tarefa.setPrioridade(switch (alerta.getSeveridade()) {
            case INFO -> com.example.sitiopro.tarefas.entity.PrioridadeTarefa.BAIXA;
            case ATENCAO -> com.example.sitiopro.tarefas.entity.PrioridadeTarefa.NORMAL;
            case ALTA -> com.example.sitiopro.tarefas.entity.PrioridadeTarefa.ALTA;
            case CRITICA -> com.example.sitiopro.tarefas.entity.PrioridadeTarefa.CRITICA;
        });
        tarefa.setResponsavel(usuario);
        tarefa.setCriadoPorUsuario(usuario);
        tarefa.setOrigem(OrigemTarefa.AUTOMATICA);
        tarefa.setModuloOrigem(alerta.getModuloOrigem());
        tarefa.setReferenciaOrigem("ALERTA:" + alerta.getId());
        tarefa.setAtivo(true);
        tarefa = tarefaRepository.save(tarefa);
        historicoService.registrarTarefa(tarefa, TipoEventoOperacional.TAREFA_CRIADA, usuario, ator.ator(),
                "Tarefa criada a partir do alerta " + alerta.getId() + ".");
        registrarLog("tarefa.created", tarefa);
        return tarefa;
    }

    private Tarefa novaTarefa(TarefaRequest request, Usuario criador, Usuario responsavel) {
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(normalizarObrigatorio(request.getTitulo(), "Título"));
        tarefa.setDescricao(normalizarOpcional(request.getDescricao()));
        tarefa.setStatus(StatusTarefa.PENDENTE);
        tarefa.setPrioridade(request.getPrioridade());
        tarefa.setDataVencimento(request.getDataVencimento());
        tarefa.setResponsavel(responsavel);
        tarefa.setCriadoPorUsuario(criador);
        tarefa.setAtivo(true);
        return tarefa;
    }

    private void aplicarAutomacao(Tarefa tarefa, TarefaAutomaticaRequest request) {
        tarefa.setTitulo(normalizarObrigatorio(request.titulo(), "Título"));
        tarefa.setDescricao(normalizarOpcional(request.descricao()));
        tarefa.setPrioridade(request.prioridade() == null
                ? com.example.sitiopro.tarefas.entity.PrioridadeTarefa.NORMAL : request.prioridade());
        tarefa.setDataVencimento(request.dataVencimento());
    }

    private TarefaRecorrencia configurarRecorrencia(Tarefa tarefa, TarefaRequest request,
            TarefaRecorrencia atual) {
        TipoRecorrencia tipo = request.getRecorrencia() == null ? TipoRecorrencia.NENHUMA : request.getRecorrencia();
        if (tipo == TipoRecorrencia.NENHUMA) {
            if (atual != null) {
                atual.setAtiva(false);
            }
            return atual;
        }
        if (tarefa.getRecorrenciaOrigem() != null) {
            throw new TarefaAlertaOperacaoException("RECORRENCIA_ENCADEADA",
                    "Uma ocorrência gerada não pode originar outra série recorrente.");
        }
        validarIntervalo(tipo, request.getIntervaloDias());
        TarefaRecorrencia recorrencia = atual == null ? new TarefaRecorrencia() : atual;
        recorrencia.setTarefaModelo(tarefa);
        recorrencia.setTipo(tipo);
        recorrencia.setIntervaloDias(tipo == TipoRecorrencia.INTERVALO_DIAS ? request.getIntervaloDias() : null);
        LocalDateTime base = tarefa.getDataVencimento() == null ? LocalDateTime.now(clock) : tarefa.getDataVencimento();
        recorrencia.setProximaOcorrenciaEm(proxima(base, tipo, request.getIntervaloDias()));
        recorrencia.setAtiva(true);
        return recorrenciaRepository.save(recorrencia);
    }

    private LocalDateTime proxima(LocalDateTime base, TipoRecorrencia tipo, Integer intervaloDias) {
        return switch (tipo) {
            case DIARIA -> base.plusDays(1);
            case SEMANAL -> base.plusWeeks(1);
            case MENSAL -> base.plusMonths(1);
            case INTERVALO_DIAS -> base.plusDays(intervaloDias);
            case NENHUMA -> throw new IllegalArgumentException("Recorrência ausente não possui próxima ocorrência.");
        };
    }

    private void validarIntervalo(TipoRecorrencia tipo, Integer intervaloDias) {
        if (tipo == TipoRecorrencia.INTERVALO_DIAS
                && (intervaloDias == null || intervaloDias < 1 || intervaloDias > 365)) {
            throw new TarefaAlertaOperacaoException("INTERVALO_INVALIDO",
                    "O intervalo da recorrência deve estar entre 1 e 365 dias.");
        }
        if (tipo != TipoRecorrencia.INTERVALO_DIAS && intervaloDias != null) {
            throw new TarefaAlertaOperacaoException("INTERVALO_INDEVIDO",
                    "Informe intervalo somente para a recorrência por intervalo de dias.");
        }
    }

    private Usuario resolverResponsavel(Long responsavelId, UsuarioAtor ator, Usuario usuarioAtor) {
        if (responsavelId == null) {
            return null;
        }
        if (!ator.admin() && !Objects.equals(responsavelId, usuarioAtor.getId())) {
            throw new TarefaAlertaOperacaoException("RESPONSAVEL_NAO_PERMITIDO",
                    "Operadores podem atribuir tarefas somente a si próprios.", HttpStatus.FORBIDDEN);
        }
        return usuarioRepository.findById(responsavelId)
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new TarefaAlertaOperacaoException("RESPONSAVEL_INVALIDO",
                        "Responsável não encontrado ou inativo."));
    }

    private Usuario buscarAtor(UsuarioAtor ator) {
        if (ator.id() != null) {
            return usuarioRepository.findById(ator.id())
                    .filter(Usuario::isAtivo)
                    .orElseThrow(() -> new TarefaAlertaOperacaoException("USUARIO_INVALIDO",
                            "Usuário autenticado não encontrado ou inativo.", HttpStatus.FORBIDDEN));
        }
        return usuarioRepository.findByLogin(ator.login())
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new TarefaAlertaOperacaoException("USUARIO_INVALIDO",
                        "Usuário autenticado não encontrado ou inativo.", HttpStatus.FORBIDDEN));
    }

    private void autorizarAlteracao(Tarefa tarefa, UsuarioAtor ator) {
        if (ator.admin()) {
            return;
        }
        Long atorId = ator.id();
        if (atorId == null) {
            Usuario usuario = buscarAtor(ator);
            atorId = usuario.getId();
        }
        boolean responsavel = tarefa.getResponsavel() == null
                || Objects.equals(tarefa.getResponsavel().getId(), atorId);
        boolean criador = Objects.equals(tarefa.getCriadoPorUsuario().getId(), atorId);
        if (!responsavel && !criador) {
            throw new TarefaAlertaOperacaoException("TAREFA_NAO_PERMITIDA",
                    "Você não pode alterar uma tarefa atribuída a outro usuário.", HttpStatus.FORBIDDEN);
        }
    }

    private TarefaDetalhe detalheAtual(Tarefa tarefa) {
        TarefaRecorrencia recorrencia = recorrenciaRepository.findByTarefaModeloId(tarefa.getId()).orElse(null);
        return detalhe(tarefa, recorrencia, LocalDateTime.now(clock));
    }

    private TarefaDetalhe detalhe(Tarefa tarefa, TarefaRecorrencia recorrencia, LocalDateTime agora) {
        return new TarefaDetalhe(
                tarefa.getId(), tarefa.getTitulo(), tarefa.getDescricao(), tarefa.getStatus(), tarefa.getPrioridade(),
                tarefa.getCriadoEm(), tarefa.getDataInicio(), tarefa.getDataVencimento(), tarefa.getDataConclusao(),
                tarefa.getResponsavel() == null ? null : tarefa.getResponsavel().getId(),
                tarefa.getResponsavel() == null ? null : tarefa.getResponsavel().getNome(),
                tarefa.getCriadoPorUsuario().getId(), tarefa.getCriadoPorUsuario().getNome(), tarefa.getOrigem(),
                tarefa.getModuloOrigem(), tarefa.getReferenciaOrigem(),
                recorrencia == null ? TipoRecorrencia.NENHUMA : recorrencia.getTipo(),
                recorrencia == null ? null : recorrencia.getIntervaloDias(),
                recorrencia == null ? null : recorrencia.getProximaOcorrenciaEm(),
                recorrencia != null && recorrencia.isAtiva(), tarefa.isAtivo(), tarefa.getVersao(),
                vencida(tarefa, agora), historicoService.listarTarefa(tarefa.getId()));
    }

    private TarefaResumo resumo(Tarefa tarefa, LocalDateTime agora) {
        return new TarefaResumo(tarefa.getId(), tarefa.getTitulo(), tarefa.getStatus(), tarefa.getPrioridade(),
                tarefa.getDataVencimento(),
                tarefa.getResponsavel() == null ? null : tarefa.getResponsavel().getId(),
                tarefa.getResponsavel() == null ? null : tarefa.getResponsavel().getNome(),
                tarefa.getOrigem(), tarefa.getModuloOrigem(), vencida(tarefa, agora));
    }

    private boolean vencida(Tarefa tarefa, LocalDateTime agora) {
        return tarefa.getDataVencimento() != null
                && tarefa.getDataVencimento().isBefore(agora)
                && !tarefa.getStatus().finalizado();
    }

    private IntervaloPrazo intervalo(PrazoTarefa prazo, LocalDateTime agora) {
        LocalDate hoje = LocalDate.now(clock);
        return switch (prazo == null ? PrazoTarefa.TODAS : prazo) {
            case TODAS, VENCIDAS -> new IntervaloPrazo(null, null);
            case HOJE -> new IntervaloPrazo(hoje.atStartOfDay(), hoje.plusDays(1).atStartOfDay());
            case PROXIMAS -> new IntervaloPrazo(hoje.plusDays(1).atStartOfDay(), hoje.plusDays(8).atStartOfDay());
        };
    }

    private Tarefa buscar(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new TarefaAlertaOperacaoException("TAREFA_NAO_ENCONTRADA",
                        "Tarefa não encontrada.", HttpStatus.NOT_FOUND));
    }

    private Tarefa buscarParaAtualizacao(Long id) {
        return tarefaRepository.buscarParaAtualizacao(id)
                .orElseThrow(() -> new TarefaAlertaOperacaoException("TAREFA_NAO_ENCONTRADA",
                        "Tarefa não encontrada.", HttpStatus.NOT_FOUND));
    }

    private String normalizarObrigatorio(String valor, String campo) {
        if (!StringUtils.hasText(valor)) {
            throw new TarefaAlertaOperacaoException("CAMPO_OBRIGATORIO", campo + " é obrigatório.");
        }
        return valor.trim();
    }

    private String normalizarOpcional(String valor) {
        return StringUtils.hasText(valor) ? valor.trim() : null;
    }

    private String limitar(String valor, int limite) {
        return valor.length() <= limite ? valor : valor.substring(0, limite);
    }

    private TarefaAlertaOperacaoException conflito(String code, String message) {
        return new TarefaAlertaOperacaoException(code, message, HttpStatus.CONFLICT);
    }

    private void registrarLog(String evento, Tarefa tarefa) {
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", evento,
                "module", "tarefas",
                "tarefa.id", tarefa.getId()))) {
            log.info("Estado da tarefa atualizado: {}.", evento);
        }
    }

    private record IntervaloPrazo(LocalDateTime inicio, LocalDateTime fim) {
    }
}
