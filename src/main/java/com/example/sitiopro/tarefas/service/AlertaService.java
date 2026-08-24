package com.example.sitiopro.tarefas.service;

import com.example.sitiopro.shared.observability.MdcScope;
import com.example.sitiopro.tarefas.dto.AlertaDetalhe;
import com.example.sitiopro.tarefas.dto.AlertaFiltro;
import com.example.sitiopro.tarefas.dto.AlertaResumo;
import com.example.sitiopro.tarefas.dto.CondicaoAlerta;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import com.example.sitiopro.tarefas.dto.TarefaDetalhe;
import com.example.sitiopro.tarefas.entity.Alerta;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.StatusAlerta;
import com.example.sitiopro.tarefas.entity.Tarefa;
import com.example.sitiopro.tarefas.entity.TipoAlerta;
import com.example.sitiopro.tarefas.entity.TipoEventoOperacional;
import com.example.sitiopro.tarefas.repository.AlertaRepository;
import com.example.sitiopro.usuario.entity.Usuario;
import com.example.sitiopro.usuario.repository.UsuarioRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AlertaService {

    private static final Logger log = LoggerFactory.getLogger(AlertaService.class);
    private static final Collection<StatusAlerta> STATUS_ABERTOS = List.of(
            StatusAlerta.ATIVO, StatusAlerta.RECONHECIDO);

    private final AlertaRepository alertaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TarefaService tarefaService;
    private final HistoricoOperacionalService historicoService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public AlertaService(AlertaRepository alertaRepository,
            UsuarioRepository usuarioRepository,
            TarefaService tarefaService,
            HistoricoOperacionalService historicoService,
            ObjectMapper objectMapper,
            Clock clock) {
        this.alertaRepository = alertaRepository;
        this.usuarioRepository = usuarioRepository;
        this.tarefaService = tarefaService;
        this.historicoService = historicoService;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<AlertaResumo> listar(AlertaFiltro filtro) {
        Page<AlertaResumo> pagina = alertaRepository.buscar(filtro.getStatus(), filtro.getSeveridade(),
                        filtro.getModulo(), filtro.isSomenteAtivos(),
                        PageRequest.of(filtro.getPagina(), filtro.getTamanho()))
                .map(this::resumo);
        return PaginaResponse.de(pagina);
    }

    @Transactional(readOnly = true)
    public AlertaDetalhe detalhar(Long id) {
        return detalhe(buscar(id));
    }

    @Transactional
    public AlertaDetalhe reconhecer(Long id, UsuarioAtor ator) {
        Alerta alerta = buscarParaAtualizacao(id);
        if (alerta.getStatus() == StatusAlerta.RESOLVIDO) {
            throw new TarefaAlertaOperacaoException("ALERTA_RESOLVIDO",
                    "Um alerta resolvido não pode ser reconhecido novamente.", HttpStatus.CONFLICT);
        }
        if (alerta.getStatus() == StatusAlerta.RECONHECIDO) {
            return detalhe(alerta);
        }
        Usuario usuario = buscarAtor(ator);
        alerta.setStatus(StatusAlerta.RECONHECIDO);
        alerta.setReconhecidoEm(LocalDateTime.now(clock));
        alerta.setReconhecidoPor(usuario);
        historicoService.registrarAlerta(alerta, TipoEventoOperacional.ALERTA_RECONHECIDO,
                usuario, ator.ator(), "Alerta reconhecido.");
        registrarLog("alerta.acknowledged", alerta);
        return detalhe(alerta);
    }

    @Transactional
    public AlertaDetalhe resolver(Long id, UsuarioAtor ator) {
        if (!ator.admin()) {
            throw new TarefaAlertaOperacaoException("ALERTA_RESOLUCAO_NAO_PERMITIDA",
                    "Somente administradores podem resolver alertas.", HttpStatus.FORBIDDEN);
        }
        Alerta alerta = buscarParaAtualizacao(id);
        if (alerta.getStatus() == StatusAlerta.RESOLVIDO) {
            return detalhe(alerta);
        }
        Usuario usuario = buscarAtor(ator);
        resolver(alerta, LocalDateTime.now(clock));
        historicoService.registrarAlerta(alerta, TipoEventoOperacional.ALERTA_RESOLVIDO,
                usuario, ator.ator(), "Alerta resolvido manualmente.");
        registrarLog("alerta.resolved", alerta);
        return detalhe(alerta);
    }

    @Transactional
    public TarefaDetalhe criarTarefa(Long id, UsuarioAtor ator) {
        Alerta alerta = buscarParaAtualizacao(id);
        if (alerta.getTarefa() != null) {
            return tarefaService.detalhar(alerta.getTarefa().getId());
        }
        Usuario usuario = buscarAtor(ator);
        Tarefa tarefa = tarefaService.criarAPartirDoAlerta(alerta, ator);
        alerta.setTarefa(tarefa);
        historicoService.registrarAlerta(alerta, TipoEventoOperacional.ALERTA_TAREFA_CRIADA,
                usuario, ator.ator(), "Tarefa " + tarefa.getId() + " criada a partir do alerta.");
        registrarLog("alerta.task_created", alerta);
        return tarefaService.detalhar(tarefa.getId());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public int sincronizar(ModuloOrigem modulo, TipoAlerta tipo, List<CondicaoAlerta> condicoes) {
        Map<String, CondicaoAlerta> detectadas = validarCondicoes(condicoes);
        List<Alerta> abertos = alertaRepository.buscarAbertosParaAtualizacao(modulo, tipo, STATUS_ABERTOS);
        Map<String, Alerta> porChave = new LinkedHashMap<>();
        abertos.forEach(alerta -> porChave.put(alerta.getChaveDeduplicacao(), alerta));
        LocalDateTime agora = LocalDateTime.now(clock);
        int alteracoes = 0;

        for (CondicaoAlerta condicao : detectadas.values()) {
            Alerta alerta = porChave.remove(condicao.chaveDeduplicacao());
            if (alerta == null) {
                alerta = criarAutomatico(modulo, tipo, condicao, agora);
                alteracoes++;
                continue;
            }
            String contexto = serializarContexto(condicao.contexto());
            boolean mudou = !Objects.equals(alerta.getTitulo(), condicao.titulo())
                    || !Objects.equals(alerta.getDescricao(), condicao.descricao())
                    || alerta.getSeveridade() != condicao.severidade()
                    || !Objects.equals(alerta.getReferenciaOrigem(), condicao.referenciaOrigem())
                    || !Objects.equals(alerta.getDadosContexto(), contexto);
            alerta.setTitulo(normalizar(condicao.titulo(), 180));
            alerta.setDescricao(normalizarOpcional(condicao.descricao(), 2000));
            alerta.setSeveridade(condicao.severidade());
            alerta.setReferenciaOrigem(normalizar(condicao.referenciaOrigem(), 160));
            alerta.setDadosContexto(contexto);
            alerta.setAtualizadoEm(agora);
            if (mudou) {
                historicoService.registrarAlerta(alerta, TipoEventoOperacional.ALERTA_ATUALIZADO,
                        null, "sistema", "Condição automática atualizada.");
                registrarLog("alerta.updated", alerta);
                alteracoes++;
            }
        }

        for (Alerta alerta : porChave.values()) {
            resolver(alerta, agora);
            historicoService.registrarAlerta(alerta, TipoEventoOperacional.ALERTA_RESOLVIDO,
                    null, "sistema", "Condição automática deixou de existir.");
            registrarLog("alerta.resolved", alerta);
            alteracoes++;
        }
        alertaRepository.flush();
        return alteracoes;
    }

    private Alerta criarAutomatico(ModuloOrigem modulo, TipoAlerta tipo, CondicaoAlerta condicao,
            LocalDateTime agora) {
        Alerta alerta = new Alerta();
        alerta.setTitulo(normalizar(condicao.titulo(), 180));
        alerta.setDescricao(normalizarOpcional(condicao.descricao(), 2000));
        alerta.setSeveridade(condicao.severidade());
        alerta.setStatus(StatusAlerta.ATIVO);
        alerta.setModuloOrigem(modulo);
        alerta.setTipo(tipo);
        alerta.setReferenciaOrigem(normalizar(condicao.referenciaOrigem(), 160));
        alerta.setChaveDeduplicacao(normalizar(condicao.chaveDeduplicacao(), 220));
        alerta.setDetectadoEm(agora);
        alerta.setAtualizadoEm(agora);
        alerta.setDadosContexto(serializarContexto(condicao.contexto()));
        alerta = alertaRepository.save(alerta);
        historicoService.registrarAlerta(alerta, TipoEventoOperacional.ALERTA_DETECTADO,
                null, "sistema", "Condição automática detectada.");
        registrarLog("alerta.detected", alerta);
        return alerta;
    }

    private void resolver(Alerta alerta, LocalDateTime agora) {
        alerta.setStatus(StatusAlerta.RESOLVIDO);
        alerta.setResolvidoEm(agora);
        alerta.setAtualizadoEm(agora);
    }

    private Map<String, CondicaoAlerta> validarCondicoes(List<CondicaoAlerta> condicoes) {
        Map<String, CondicaoAlerta> resultado = new LinkedHashMap<>();
        for (CondicaoAlerta condicao : condicoes == null ? List.<CondicaoAlerta>of() : condicoes) {
            String chave = normalizar(condicao.chaveDeduplicacao(), 220);
            if (resultado.putIfAbsent(chave, condicao) != null) {
                throw new IllegalArgumentException("A avaliação automática produziu chave de alerta duplicada.");
            }
        }
        return resultado;
    }

    private String serializarContexto(Map<String, Object> contexto) {
        if (contexto == null || contexto.isEmpty()) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(contexto);
            if (json.length() > 1000) {
                throw new IllegalArgumentException("Contexto do alerta excede o limite seguro.");
            }
            return json;
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Contexto do alerta não pôde ser serializado.", ex);
        }
    }

    private Map<String, Object> contexto(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            return Map.of("indisponivel", true);
        }
    }

    private AlertaResumo resumo(Alerta alerta) {
        return new AlertaResumo(alerta.getId(), alerta.getTitulo(), alerta.getSeveridade(), alerta.getStatus(),
                alerta.getModuloOrigem(), alerta.getTipo(), alerta.getReferenciaOrigem(), alerta.getDetectadoEm(),
                alerta.getAtualizadoEm(), alerta.getTarefa() == null ? null : alerta.getTarefa().getId());
    }

    private AlertaDetalhe detalhe(Alerta alerta) {
        return new AlertaDetalhe(alerta.getId(), alerta.getTitulo(), alerta.getDescricao(), alerta.getSeveridade(),
                alerta.getStatus(), alerta.getModuloOrigem(), alerta.getTipo(), alerta.getReferenciaOrigem(),
                alerta.getChaveDeduplicacao(), alerta.getDetectadoEm(), alerta.getAtualizadoEm(),
                alerta.getReconhecidoEm(),
                alerta.getReconhecidoPor() == null ? null : alerta.getReconhecidoPor().getNome(),
                alerta.getResolvidoEm(), contexto(alerta.getDadosContexto()),
                alerta.getTarefa() == null ? null : alerta.getTarefa().getId(), alerta.getVersao(),
                historicoService.listarAlerta(alerta.getId()));
    }

    private Usuario buscarAtor(UsuarioAtor ator) {
        if (ator.id() != null) {
            return usuarioRepository.findById(ator.id()).filter(Usuario::isAtivo)
                    .orElseThrow(this::usuarioInvalido);
        }
        return usuarioRepository.findByLogin(ator.login()).filter(Usuario::isAtivo)
                .orElseThrow(this::usuarioInvalido);
    }

    private TarefaAlertaOperacaoException usuarioInvalido() {
        return new TarefaAlertaOperacaoException("USUARIO_INVALIDO",
                "Usuário autenticado não encontrado ou inativo.", HttpStatus.FORBIDDEN);
    }

    private Alerta buscar(Long id) {
        return alertaRepository.findById(id)
                .orElseThrow(() -> new TarefaAlertaOperacaoException("ALERTA_NAO_ENCONTRADO",
                        "Alerta não encontrado.", HttpStatus.NOT_FOUND));
    }

    private Alerta buscarParaAtualizacao(Long id) {
        return alertaRepository.buscarParaAtualizacao(id)
                .orElseThrow(() -> new TarefaAlertaOperacaoException("ALERTA_NAO_ENCONTRADO",
                        "Alerta não encontrado.", HttpStatus.NOT_FOUND));
    }

    private String normalizar(String valor, int limite) {
        if (!StringUtils.hasText(valor)) {
            throw new IllegalArgumentException("Texto obrigatório ausente na condição de alerta.");
        }
        String texto = valor.trim();
        return texto.length() <= limite ? texto : texto.substring(0, limite);
    }

    private String normalizarOpcional(String valor, int limite) {
        if (!StringUtils.hasText(valor)) {
            return null;
        }
        String texto = valor.trim();
        return texto.length() <= limite ? texto : texto.substring(0, limite);
    }

    private void registrarLog(String evento, Alerta alerta) {
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", evento,
                "module", "tarefas",
                "alerta.id", alerta.getId(),
                "alerta.modulo", alerta.getModuloOrigem()))) {
            log.info("Estado do alerta atualizado: {}.", evento);
        }
    }
}
