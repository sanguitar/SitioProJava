package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalService;
import com.example.sitiopro.criacao.aves.config.AvesProperties;
import com.example.sitiopro.criacao.aves.dto.AcompanhamentoIncubacaoAvesResumo;
import com.example.sitiopro.criacao.aves.dto.AjustarPrevisaoIncubacaoAvesRequest;
import com.example.sitiopro.criacao.aves.dto.CriarIncubacaoAvesRequest;
import com.example.sitiopro.criacao.aves.dto.CriarLoteAvesRequest;
import com.example.sitiopro.criacao.aves.dto.FinalizarIncubacaoAvesRequest;
import com.example.sitiopro.criacao.aves.dto.IncubacaoAvesDetalhe;
import com.example.sitiopro.criacao.aves.dto.IncubacaoAvesResumo;
import com.example.sitiopro.criacao.aves.dto.PosturaOrigemAvesResumo;
import com.example.sitiopro.criacao.aves.entity.EspecieAves;
import com.example.sitiopro.criacao.aves.entity.FinalidadeLoteAves;
import com.example.sitiopro.criacao.aves.entity.IncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.LoteAves;
import com.example.sitiopro.criacao.aves.entity.MetodoIncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.RegistroPosturaAves;
import com.example.sitiopro.criacao.aves.entity.SexoLoteAves;
import com.example.sitiopro.criacao.aves.entity.StatusIncubacaoAves;
import com.example.sitiopro.criacao.aves.repository.IncubacaoAvesRepository;
import com.example.sitiopro.criacao.aves.repository.RegistroPosturaAvesRepository;
import com.example.sitiopro.criacao.core.entity.InstalacaoCriacao;
import com.example.sitiopro.criacao.core.entity.TipoInstalacaoCriacao;
import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.shared.observability.MdcScope;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class IncubacaoAvesService {

    private static final Logger log = LoggerFactory.getLogger(IncubacaoAvesService.class);

    private final IncubacaoAvesRepository repository;
    private final InstalacaoCriacaoService instalacaoService;
    private final LoteAvesService loteService;
    private final AvesAlertasService alertasService;
    private final CodigoCriacaoService codigoService;
    private final RegistroPosturaAvesRepository posturaRepository;
    private final IncubacaoAcompanhamentoService acompanhamentoService;
    private final IncubacaoOperacionalService operacionalService;
    private final ConfiguracaoOperacionalService configuracaoOperacionalService;
    private final AvesProperties properties;
    private final Clock clock;

    public IncubacaoAvesService(IncubacaoAvesRepository repository,
            InstalacaoCriacaoService instalacaoService,
            LoteAvesService loteService,
            AvesAlertasService alertasService,
            CodigoCriacaoService codigoService,
            RegistroPosturaAvesRepository posturaRepository,
            IncubacaoAcompanhamentoService acompanhamentoService,
            IncubacaoOperacionalService operacionalService,
            ConfiguracaoOperacionalService configuracaoOperacionalService,
            AvesProperties properties,
            Clock clock) {
        this.repository = repository;
        this.instalacaoService = instalacaoService;
        this.loteService = loteService;
        this.alertasService = alertasService;
        this.codigoService = codigoService;
        this.posturaRepository = posturaRepository;
        this.acompanhamentoService = acompanhamentoService;
        this.operacionalService = operacionalService;
        this.configuracaoOperacionalService = configuracaoOperacionalService;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<IncubacaoAvesResumo> listar(int pagina, int tamanho) {
        return PaginaResponse.de(repository.findAllByOrderByDataInicioDescIdDesc(
                PageRequest.of(Math.max(0, pagina), Math.min(100, Math.max(1, tamanho))))
                .map(this::resumo));
    }

    @Transactional(readOnly = true)
    public IncubacaoAvesDetalhe detalhar(Long id) {
        return detalhe(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<PosturaOrigemAvesResumo> listarPosturasRecentes() {
        return posturaRepository.findTop50ByOrderByDataColetaDescIdDesc().stream()
                .map(postura -> new PosturaOrigemAvesResumo(postura.getId(), postura.getLote().getId(),
                        postura.getLote().getCodigo(), postura.getDataColeta(), postura.getOvosInteiros()))
                .toList();
    }

    public Map<EspecieAves, Integer> periodosIncubacao() {
        Map<EspecieAves, Integer> periodos = new LinkedHashMap<>(properties.getPeriodosIncubacaoDias());
        periodos.put(EspecieAves.GALINHA, configuracaoOperacionalService.obter().diasPadraoIncubacao());
        return Map.copyOf(periodos);
    }

    public LocalDate previsaoPadrao(EspecieAves especie, LocalDate inicio) {
        if (inicio == null || especie == null) return null;
        Integer dias = especie == EspecieAves.GALINHA
                ? Integer.valueOf(configuracaoOperacionalService.obter().diasPadraoIncubacao())
                : properties.getPeriodosIncubacaoDias().get(especie);
        return dias == null ? null : inicio.plusDays(dias);
    }

    @Transactional
    public IncubacaoAvesDetalhe criar(CriarIncubacaoAvesRequest request, UsuarioAtor ator) {
        long inicioOperacao = System.nanoTime();
        String chave = obrigatorio(request.getChaveIdempotencia(), "Chave de idempotência");
        codigoService.bloquearIdempotencia("INCUBACAO_AVES", chave);
        IncubacaoAves existente = repository.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) {
            operacionalService.garantirTarefas(existente, ator);
            return detalhe(existente);
        }

        validarPrincipal(request);
        InstalacaoCriacao instalacao = instalacaoService.buscarAtiva(request.getInstalacaoId());
        validarInstalacao(request.getMetodo(), instalacao);
        RegistroPosturaAves postura = buscarPostura(request.getPosturaOrigemId());
        LoteAves reprodutor = buscarOrigem(request.getLoteReprodutorId(), postura);
        validarEspecieOrigem(request.getEspecie(), reprodutor);
        LocalDate dataPrevista = resolverPrevisao(request);

        IncubacaoAves incubacao = new IncubacaoAves();
        incubacao.setCodigo(codigoService.proximaIncubacaoAves());
        incubacao.setMetodo(request.getMetodo());
        incubacao.setEspecie(request.getEspecie());
        incubacao.setInstalacao(instalacao);
        incubacao.setDataInicio(request.getDataInicio());
        incubacao.setQuantidadeOvos(request.getQuantidadeOvos());
        incubacao.setOrigemOvos(texto(request.getOrigemOvos()));
        incubacao.setLoteReprodutor(reprodutor);
        incubacao.setPosturaOrigem(postura);
        incubacao.setDataPrevistaEclosao(dataPrevista);
        incubacao.setStatus(StatusIncubacaoAves.EM_INCUBACAO);
        incubacao.setObservacao(texto(request.getObservacao()));
        incubacao.setChaveIdempotencia(chave);
        incubacao = repository.save(incubacao);

        operacionalService.garantirTarefas(incubacao, ator);
        alertasService.avaliar();
        registrarLog("criacao.aves.incubation.started", incubacao, request.getQuantidadeOvos(), inicioOperacao);
        return detalhe(incubacao);
    }

    @Transactional
    public IncubacaoAvesDetalhe finalizar(Long id, FinalizarIncubacaoAvesRequest request, UsuarioAtor ator) {
        long inicioOperacao = System.nanoTime();
        IncubacaoAves incubacao = buscarParaAtualizacao(id);
        if (incubacao.getStatus() == StatusIncubacaoAves.FINALIZADA) return detalhe(incubacao);
        if (incubacao.getStatus() == StatusIncubacaoAves.CANCELADA) {
            throw conflito("INCUBACAO_CANCELADA", "Incubação cancelada não pode ser finalizada.");
        }
        validarFinalizacao(incubacao, request);
        incubacao.setPintinhosEclodidos(request.getPintinhosEclodidos());
        incubacao.setOvosPerdidos(request.getOvosPerdidos());
        incubacao.setDataEclosao(request.getDataEclosao());
        incubacao.setObservacaoFinalizacao(texto(request.getObservacao()));
        if (request.isCriarLote() && request.getPintinhosEclodidos() > 0) {
            incubacao.setLoteResultante(loteService.criarDeIncubacao(
                    loteResultante(incubacao, request), ator.ator(), incubacao.getId()));
        }
        incubacao.setStatus(StatusIncubacaoAves.FINALIZADA);
        alertasService.avaliar();
        registrarLog("criacao.aves.incubation.finished", incubacao,
                request.getPintinhosEclodidos(), inicioOperacao);
        return detalhe(incubacao);
    }

    @Transactional
    public IncubacaoAvesDetalhe cancelar(Long id, UsuarioAtor ator) {
        exigirAdmin(ator, "Somente administradores podem cancelar incubação.");
        long inicioOperacao = System.nanoTime();
        IncubacaoAves incubacao = buscarParaAtualizacao(id);
        if (incubacao.getStatus() == StatusIncubacaoAves.CANCELADA) return detalhe(incubacao);
        if (incubacao.getStatus() == StatusIncubacaoAves.FINALIZADA) {
            throw conflito("INCUBACAO_FINALIZADA", "Incubação finalizada não pode ser cancelada.");
        }
        incubacao.setStatus(StatusIncubacaoAves.CANCELADA);
        alertasService.avaliar();
        registrarLog("criacao.aves.incubation.cancelled", incubacao, null, inicioOperacao);
        return detalhe(incubacao);
    }

    @Transactional
    public IncubacaoAvesDetalhe ajustarPrevisao(Long id, AjustarPrevisaoIncubacaoAvesRequest request,
            UsuarioAtor ator) {
        exigirAdmin(ator, "Somente administradores podem ajustar a previsão de eclosão.");
        IncubacaoAves incubacao = buscarParaAtualizacao(id);
        if (incubacao.getStatus() != StatusIncubacaoAves.EM_INCUBACAO) {
            throw conflito("INCUBACAO_ENCERRADA", "A previsão só pode ser ajustada durante a incubação.");
        }
        if (request.getDataPrevistaEclosao() == null
                || request.getDataPrevistaEclosao().isBefore(incubacao.getDataInicio())) {
            throw erro("PREVISAO_ECLOSAO_INVALIDA", "Informe uma previsão igual ou posterior ao início.");
        }
        incubacao.setDataPrevistaEclosao(request.getDataPrevistaEclosao());
        incubacao.setMotivoAjustePrevisao(obrigatorio(request.getMotivo(), "Motivo do ajuste"));
        operacionalService.garantirTarefas(incubacao, ator);
        alertasService.avaliar();
        registrarLog("criacao.aves.incubation.forecast_adjusted", incubacao, null, System.nanoTime());
        return detalhe(incubacao);
    }

    private CriarLoteAvesRequest loteResultante(IncubacaoAves incubacao, FinalizarIncubacaoAvesRequest request) {
        if (request.getInstalacaoDestinoId() == null || !StringUtils.hasText(request.getChaveIdempotenciaLote())) {
            throw erro("LOTE_RESULTANTE_INCOMPLETO",
                    "Informe instalação e chave idempotente do lote de pintinhos.");
        }
        CriarLoteAvesRequest lote = new CriarLoteAvesRequest();
        lote.setNome(request.getNomeLote());
        lote.setEspecie(incubacao.getEspecie());
        lote.setFinalidade(request.getFinalidadeLote() == null
                ? FinalidadeLoteAves.MISTA : request.getFinalidadeLote());
        lote.setOrigem("Incubação " + incubacao.getCodigo());
        lote.setDataEntrada(request.getDataEclosao());
        lote.setDataNascimento(request.getDataEclosao());
        lote.setQuantidadeInicial(request.getPintinhosEclodidos());
        lote.setSexo(request.getSexoLote() == null ? SexoLoteAves.MISTO : request.getSexoLote());
        lote.setInstalacaoId(request.getInstalacaoDestinoId());
        lote.setObservacoes("Lote resultante da incubação #" + incubacao.getId());
        lote.setChaveIdempotencia(request.getChaveIdempotenciaLote());
        return lote;
    }

    private void validarPrincipal(CriarIncubacaoAvesRequest request) {
        if (request.getMetodo() == null || request.getEspecie() == null || request.getInstalacaoId() == null) {
            throw erro("DADOS_PRINCIPAIS_OBRIGATORIOS", "Informe método, espécie e instalação.");
        }
        if (request.getQuantidadeOvos() == null || request.getQuantidadeOvos() < 1) {
            throw erro("OVOS_QUANTIDADE_INVALIDA", "Quantidade de ovos deve ser maior que zero.");
        }
        if (request.getDataInicio() == null) {
            throw erro("DATA_INICIO_OBRIGATORIA", "Informe a data de início da incubação.");
        }
    }

    private void validarInstalacao(MetodoIncubacaoAves metodo, InstalacaoCriacao instalacao) {
        if (metodo == MetodoIncubacaoAves.CHOCADEIRA
                && instalacao.getTipo() != TipoInstalacaoCriacao.INCUBADORA) {
            throw erro("INSTALACAO_NAO_INCUBADORA",
                    "Para o método chocadeira, selecione uma instalação do tipo incubadora.");
        }
    }

    private RegistroPosturaAves buscarPostura(Long posturaId) {
        if (posturaId == null) return null;
        return posturaRepository.findById(posturaId)
                .orElseThrow(() -> erro("POSTURA_ORIGEM_INVALIDA", "Postura de origem não encontrada."));
    }

    private LoteAves buscarOrigem(Long loteId, RegistroPosturaAves postura) {
        LoteAves lote = loteId == null ? null : loteService.buscar(loteId);
        if (postura == null) return lote;
        if (lote != null && !lote.getId().equals(postura.getLote().getId())) {
            throw erro("ORIGEM_OVOS_INCOERENTE", "A postura selecionada não pertence ao lote de origem.");
        }
        return postura.getLote();
    }

    private void validarEspecieOrigem(EspecieAves especie, LoteAves lote) {
        if (lote != null && lote.getEspecie() != especie) {
            throw erro("ESPECIE_ORIGEM_INCOERENTE", "A espécie deve ser a mesma do lote de origem.");
        }
    }

    private LocalDate resolverPrevisao(CriarIncubacaoAvesRequest request) {
        LocalDate prevista = previsaoPadrao(request.getEspecie(), request.getDataInicio());
        if (prevista == null) {
            throw erro("PERIODO_INCUBACAO_NAO_CONFIGURADO",
                    "O período da espécie não está configurado. Solicite o ajuste a um administrador.");
        }
        if (prevista.isBefore(request.getDataInicio())) {
            throw erro("PREVISAO_ECLOSAO_INVALIDA", "Previsão de eclosão não pode ser anterior ao início.");
        }
        return prevista;
    }

    private void validarFinalizacao(IncubacaoAves incubacao, FinalizarIncubacaoAvesRequest request) {
        if (request.getPintinhosEclodidos() == null || request.getOvosPerdidos() == null
                || request.getPintinhosEclodidos() < 0 || request.getOvosPerdidos() < 0) {
            throw erro("RESULTADO_INCUBACAO_INVALIDO", "Eclodidos e perdas não podem ser negativos.");
        }
        if (request.getPintinhosEclodidos() + request.getOvosPerdidos() > incubacao.getQuantidadeOvos()) {
            throw erro("RESULTADO_SUPERA_OVOS", "Eclodidos e perdas não podem superar os ovos incubados.");
        }
        if (request.getDataEclosao() == null || request.getDataEclosao().isBefore(incubacao.getDataInicio())) {
            throw erro("DATA_ECLOSAO_INVALIDA", "Informe uma data real igual ou posterior ao início.");
        }
    }

    private IncubacaoAves buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new AvesOperacaoException(
                "INCUBACAO_NAO_ENCONTRADA", "Incubação não encontrada.", HttpStatus.NOT_FOUND));
    }

    private IncubacaoAves buscarParaAtualizacao(Long id) {
        return repository.buscarParaAtualizacao(id).orElseThrow(() -> new AvesOperacaoException(
                "INCUBACAO_NAO_ENCONTRADA", "Incubação não encontrada.", HttpStatus.NOT_FOUND));
    }

    private IncubacaoAvesResumo resumo(IncubacaoAves incubacao) {
        Progresso progresso = progresso(incubacao);
        return new IncubacaoAvesResumo(incubacao.getId(), incubacao.getCodigo(), incubacao.getMetodo(),
                incubacao.getEspecie(), incubacao.getInstalacao().getId(), incubacao.getInstalacao().getNome(),
                incubacao.getDataInicio(), incubacao.getQuantidadeOvos(), incubacao.getDataPrevistaEclosao(),
                incubacao.getStatus(), incubacao.getPintinhosEclodidos(), incubacao.getOvosPerdidos(),
                taxa(incubacao.getPintinhosEclodidos(), incubacao.getQuantidadeOvos()),
                taxa(incubacao.getOvosPerdidos(), incubacao.getQuantidadeOvos()),
                incubacao.getLoteResultante() == null ? null : incubacao.getLoteResultante().getId(),
                progresso.diasRestantes(), progresso.percentual());
    }

    private IncubacaoAvesDetalhe detalhe(IncubacaoAves incubacao) {
        List<AcompanhamentoIncubacaoAvesResumo> acompanhamentos = acompanhamentoService.listar(incubacao.getId());
        AcompanhamentoIncubacaoAvesResumo ultimaMedicao = acompanhamentos.stream()
                .filter(item -> item.temperatura() != null || item.umidade() != null)
                .findFirst().orElse(null);
        Progresso progresso = progresso(incubacao);
        return new IncubacaoAvesDetalhe(
                incubacao.getId(), incubacao.getCodigo(), incubacao.getMetodo(), incubacao.getEspecie(),
                incubacao.getInstalacao().getId(), incubacao.getInstalacao().getNome(), incubacao.getDataInicio(),
                incubacao.getQuantidadeOvos(), incubacao.getOrigemOvos(),
                incubacao.getLoteReprodutor() == null ? null : incubacao.getLoteReprodutor().getId(),
                incubacao.getLoteReprodutor() == null ? null : incubacao.getLoteReprodutor().getCodigo(),
                incubacao.getPosturaOrigem() == null ? null : incubacao.getPosturaOrigem().getId(),
                incubacao.getPosturaOrigem() == null ? null : incubacao.getPosturaOrigem().getDataColeta(),
                incubacao.getDataPrevistaEclosao(), incubacao.getStatus(), incubacao.getObservacao(),
                incubacao.getMotivoAjustePrevisao(), incubacao.getPintinhosEclodidos(), incubacao.getOvosPerdidos(),
                incubacao.getDataEclosao(), incubacao.getObservacaoFinalizacao(),
                taxa(incubacao.getPintinhosEclodidos(), incubacao.getQuantidadeOvos()),
                taxa(incubacao.getOvosPerdidos(), incubacao.getQuantidadeOvos()),
                incubacao.getLoteResultante() == null ? null : incubacao.getLoteResultante().getId(),
                incubacao.getLoteResultante() == null ? null : incubacao.getLoteResultante().getCodigo(),
                progresso.periodo(), progresso.diasDecorridos(), progresso.diasRestantes(), progresso.diaAtual(),
                progresso.percentual(), ultimaMedicao == null ? null : ultimaMedicao.temperatura(),
                ultimaMedicao == null ? null : ultimaMedicao.umidade(),
                ultimaMedicao == null ? null : ultimaMedicao.dataHora(), acompanhamentos,
                operacionalService.tarefas(incubacao.getId()), operacionalService.alertas(incubacao.getId()),
                incubacao.getVersao(), incubacao.getCriadoEm(), incubacao.getCriadoPor(),
                incubacao.getAlteradoEm(), incubacao.getAlteradoPor());
    }

    private Progresso progresso(IncubacaoAves incubacao) {
        LocalDate hoje = LocalDate.now(clock);
        LocalDate referencia = incubacao.getStatus() == StatusIncubacaoAves.FINALIZADA
                ? incubacao.getDataEclosao() : hoje;
        long periodo = Math.max(1, ChronoUnit.DAYS.between(
                incubacao.getDataInicio(), incubacao.getDataPrevistaEclosao()));
        long decorridos = Math.max(0, ChronoUnit.DAYS.between(incubacao.getDataInicio(), referencia));
        long restantes = ChronoUnit.DAYS.between(hoje, incubacao.getDataPrevistaEclosao());
        long diaAtual = referencia.isBefore(incubacao.getDataInicio()) ? 0 : decorridos + 1;
        int percentual = (int) Math.min(100, (decorridos * 100) / periodo);
        return new Progresso(periodo, decorridos, restantes, diaAtual, percentual);
    }

    private BigDecimal taxa(Integer valor, int total) {
        return valor == null ? null : BigDecimal.valueOf(valor).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private String texto(String valor) { return StringUtils.hasText(valor) ? valor.trim() : null; }
    private String obrigatorio(String valor, String campo) {
        if (!StringUtils.hasText(valor)) throw erro("CAMPO_OBRIGATORIO", campo + " é obrigatório.");
        return valor.trim();
    }
    private void exigirAdmin(UsuarioAtor ator, String mensagem) {
        if (!ator.admin()) throw new AvesOperacaoException("OPERACAO_ADMIN_OBRIGATORIA", mensagem, HttpStatus.FORBIDDEN);
    }
    private AvesOperacaoException erro(String codigo, String mensagem) {
        return new AvesOperacaoException(codigo, mensagem);
    }
    private AvesOperacaoException conflito(String codigo, String mensagem) {
        return new AvesOperacaoException(codigo, mensagem, HttpStatus.CONFLICT);
    }

    private void registrarLog(String evento, IncubacaoAves incubacao, Integer quantidade, long inicio) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("event.action", evento);
        dados.put("module", "criacoes");
        dados.put("criacao.aves.incubacao.id", String.valueOf(incubacao.getId()));
        dados.put("event.duration", System.nanoTime() - inicio);
        if (quantidade != null) dados.put("criacao.quantidade", quantidade);
        try (MdcScope ignored = MdcScope.with(dados)) {
            log.info("Operação de incubação concluída.");
        }
    }

    private record Progresso(long periodo, long diasDecorridos, long diasRestantes,
            long diaAtual, int percentual) {
    }
}
