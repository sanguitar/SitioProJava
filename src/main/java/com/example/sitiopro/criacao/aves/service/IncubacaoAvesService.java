package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.criacao.aves.dto.*;
import com.example.sitiopro.criacao.aves.entity.*;
import com.example.sitiopro.criacao.aves.repository.IncubacaoAvesRepository;
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
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class IncubacaoAvesService {
    private static final Logger log = LoggerFactory.getLogger(IncubacaoAvesService.class);
    private final IncubacaoAvesRepository repository;
    private final InstalacaoCriacaoService instalacaoService;
    private final LoteAvesService loteService;
    private final AvesAlertasService alertasService;
    private final CodigoCriacaoService codigoService;
    private final Clock clock;

    public IncubacaoAvesService(IncubacaoAvesRepository repository, InstalacaoCriacaoService instalacaoService,
            LoteAvesService loteService, AvesAlertasService alertasService,
            CodigoCriacaoService codigoService, Clock clock) {
        this.repository = repository; this.instalacaoService = instalacaoService; this.loteService = loteService;
        this.alertasService = alertasService; this.codigoService = codigoService; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<IncubacaoAvesResumo> listar(int pagina, int tamanho) {
        return PaginaResponse.de(repository.findAllByOrderByDataInicioDescIdDesc(
                PageRequest.of(Math.max(0, pagina), Math.min(100, Math.max(1, tamanho)))).map(this::resumo));
    }

    @Transactional(readOnly = true)
    public IncubacaoAvesDetalhe detalhar(Long id) { return detalhe(buscar(id)); }

    @Transactional
    public IncubacaoAvesDetalhe criar(CriarIncubacaoAvesRequest request, UsuarioAtor ator) {
        long inicio = System.nanoTime(); String chave = obrigatorio(request.getChaveIdempotencia(), "Chave de idempotência");
        codigoService.bloquearIdempotencia("INCUBACAO_AVES", chave);
        IncubacaoAves existente = repository.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) return detalhe(existente);
        validar(request);
        String codigo = codigoService.proximaIncubacaoAves();
        InstalacaoCriacao instalacao = instalacaoService.buscarAtiva(request.getInstalacaoId());
        if (instalacao.getTipo() != TipoInstalacaoCriacao.INCUBADORA) throw erro("INSTALACAO_NAO_INCUBADORA", "Selecione uma instalação do tipo incubadora.");
        LoteAves reprodutor = request.getLoteReprodutorId() == null ? null : loteService.buscar(request.getLoteReprodutorId());
        IncubacaoAves incubacao = new IncubacaoAves(); incubacao.setCodigo(codigo); incubacao.setInstalacao(instalacao);
        incubacao.setDataInicio(request.getDataInicio()); incubacao.setQuantidadeOvos(request.getQuantidadeOvos());
        incubacao.setOrigemOvos(texto(request.getOrigemOvos())); incubacao.setLoteReprodutor(reprodutor);
        incubacao.setDataPrevistaEclosao(request.getDataPrevistaEclosao()); incubacao.setStatus(StatusIncubacaoAves.EM_INCUBACAO);
        incubacao.setObservacao(texto(request.getObservacao())); incubacao.setChaveIdempotencia(chave);
        incubacao = repository.save(incubacao); alertasService.avaliar();
        log("criacao.aves.incubation.started", incubacao, request.getQuantidadeOvos(), inicio);
        return detalhe(incubacao);
    }

    @Transactional
    public IncubacaoAvesDetalhe finalizar(Long id, FinalizarIncubacaoAvesRequest request, UsuarioAtor ator) {
        long inicio = System.nanoTime(); IncubacaoAves incubacao = buscarParaAtualizacao(id);
        if (incubacao.getStatus() == StatusIncubacaoAves.FINALIZADA) return detalhe(incubacao);
        if (incubacao.getStatus() == StatusIncubacaoAves.CANCELADA) throw conflito("INCUBACAO_CANCELADA", "Incubação cancelada não pode ser finalizada.");
        validarFinalizacao(incubacao, request);
        incubacao.setPintinhosEclodidos(request.getPintinhosEclodidos()); incubacao.setOvosPerdidos(request.getOvosPerdidos());
        incubacao.setDataEclosao(request.getDataEclosao());
        if (request.isCriarLote() && request.getPintinhosEclodidos() > 0) {
            CriarLoteAvesRequest lote = loteResultante(incubacao, request);
            incubacao.setLoteResultante(loteService.criarDeIncubacao(lote, ator.ator(), incubacao.getId()));
        }
        incubacao.setStatus(StatusIncubacaoAves.FINALIZADA); alertasService.avaliar();
        log("criacao.aves.incubation.finished", incubacao, request.getPintinhosEclodidos(), inicio);
        return detalhe(incubacao);
    }

    @Transactional
    public IncubacaoAvesDetalhe cancelar(Long id, UsuarioAtor ator) {
        if (!ator.admin()) throw new AvesOperacaoException("CANCELAMENTO_ADMIN_OBRIGATORIO", "Somente administradores podem cancelar incubação.", HttpStatus.FORBIDDEN);
        long inicio = System.nanoTime(); IncubacaoAves incubacao = buscarParaAtualizacao(id);
        if (incubacao.getStatus() == StatusIncubacaoAves.CANCELADA) return detalhe(incubacao);
        if (incubacao.getStatus() == StatusIncubacaoAves.FINALIZADA) throw conflito("INCUBACAO_FINALIZADA", "Incubação finalizada não pode ser cancelada.");
        incubacao.setStatus(StatusIncubacaoAves.CANCELADA); alertasService.avaliar();
        log("criacao.aves.incubation.cancelled", incubacao, null, inicio); return detalhe(incubacao);
    }

    private CriarLoteAvesRequest loteResultante(IncubacaoAves i, FinalizarIncubacaoAvesRequest r) {
        if (r.getInstalacaoDestinoId() == null || !StringUtils.hasText(r.getChaveIdempotenciaLote()))
            throw erro("LOTE_RESULTANTE_INCOMPLETO", "Informe instalação e chave idempotente do lote de pintinhos.");
        CriarLoteAvesRequest lote = new CriarLoteAvesRequest(); lote.setNome(r.getNomeLote());
        lote.setEspecie(i.getLoteReprodutor() == null ? EspecieAves.GALINHA : i.getLoteReprodutor().getEspecie());
        lote.setFinalidade(r.getFinalidadeLote() == null ? FinalidadeLoteAves.MISTA : r.getFinalidadeLote());
        lote.setOrigem("Incubação " + i.getCodigo()); lote.setDataEntrada(r.getDataEclosao()); lote.setDataNascimento(r.getDataEclosao());
        lote.setQuantidadeInicial(r.getPintinhosEclodidos()); lote.setSexo(r.getSexoLote() == null ? SexoLoteAves.MISTO : r.getSexoLote());
        lote.setInstalacaoId(r.getInstalacaoDestinoId()); lote.setObservacoes("Lote resultante da incubação #" + i.getId());
        lote.setChaveIdempotencia(r.getChaveIdempotenciaLote()); return lote;
    }

    private void validar(CriarIncubacaoAvesRequest r) { if (r.getQuantidadeOvos() == null || r.getQuantidadeOvos() < 1) throw erro("OVOS_QUANTIDADE_INVALIDA", "Quantidade de ovos deve ser maior que zero."); if (r.getDataInicio() == null || r.getDataPrevistaEclosao() == null) throw erro("DATAS_OBRIGATORIAS", "Informe as datas da incubação."); if (r.getDataPrevistaEclosao().isBefore(r.getDataInicio())) throw erro("PREVISAO_ECLOSAO_INVALIDA", "Previsão de eclosão não pode ser anterior ao início."); }
    private void validarFinalizacao(IncubacaoAves i, FinalizarIncubacaoAvesRequest r) { if (r.getPintinhosEclodidos() == null || r.getOvosPerdidos() == null || r.getPintinhosEclodidos() < 0 || r.getOvosPerdidos() < 0) throw erro("RESULTADO_INCUBACAO_INVALIDO", "Eclodidos e perdas não podem ser negativos."); if (r.getPintinhosEclodidos() + r.getOvosPerdidos() > i.getQuantidadeOvos()) throw erro("RESULTADO_SUPERA_OVOS", "Eclodidos e perdas não podem superar os ovos incubados."); if (r.getDataEclosao() == null || r.getDataEclosao().isBefore(i.getDataInicio())) throw erro("DATA_ECLOSAO_INVALIDA", "Informe uma data de eclosão válida."); }
    private IncubacaoAves buscar(Long id) { return repository.findById(id).orElseThrow(() -> new AvesOperacaoException("INCUBACAO_NAO_ENCONTRADA", "Incubação não encontrada.", HttpStatus.NOT_FOUND)); }
    private IncubacaoAves buscarParaAtualizacao(Long id) { return repository.buscarParaAtualizacao(id).orElseThrow(() -> new AvesOperacaoException("INCUBACAO_NAO_ENCONTRADA", "Incubação não encontrada.", HttpStatus.NOT_FOUND)); }
    private IncubacaoAvesResumo resumo(IncubacaoAves i) { return new IncubacaoAvesResumo(i.getId(), i.getCodigo(), i.getInstalacao().getId(), i.getInstalacao().getNome(), i.getDataInicio(), i.getQuantidadeOvos(), i.getDataPrevistaEclosao(), i.getStatus(), i.getPintinhosEclodidos(), i.getOvosPerdidos(), taxa(i.getPintinhosEclodidos(), i.getQuantidadeOvos()), taxa(i.getOvosPerdidos(), i.getQuantidadeOvos()), i.getLoteResultante() == null ? null : i.getLoteResultante().getId()); }
    private IncubacaoAvesDetalhe detalhe(IncubacaoAves i) { return new IncubacaoAvesDetalhe(i.getId(), i.getCodigo(), i.getInstalacao().getId(), i.getInstalacao().getNome(), i.getDataInicio(), i.getQuantidadeOvos(), i.getOrigemOvos(), i.getLoteReprodutor() == null ? null : i.getLoteReprodutor().getId(), i.getLoteReprodutor() == null ? null : i.getLoteReprodutor().getCodigo(), i.getDataPrevistaEclosao(), i.getStatus(), i.getObservacao(), i.getPintinhosEclodidos(), i.getOvosPerdidos(), i.getDataEclosao(), taxa(i.getPintinhosEclodidos(), i.getQuantidadeOvos()), taxa(i.getOvosPerdidos(), i.getQuantidadeOvos()), i.getLoteResultante() == null ? null : i.getLoteResultante().getId(), i.getLoteResultante() == null ? null : i.getLoteResultante().getCodigo(), i.getVersao(), i.getCriadoEm(), i.getCriadoPor(), i.getAlteradoEm(), i.getAlteradoPor()); }
    private BigDecimal taxa(Integer valor, int total) { return valor == null ? null : BigDecimal.valueOf(valor).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP); }
    private String texto(String v) { return StringUtils.hasText(v) ? v.trim() : null; }
    private String obrigatorio(String v, String c) { if (!StringUtils.hasText(v)) throw erro("CAMPO_OBRIGATORIO", c + " é obrigatório."); return v.trim(); }
    private AvesOperacaoException erro(String c, String m) { return new AvesOperacaoException(c, m); }
    private AvesOperacaoException conflito(String c, String m) { return new AvesOperacaoException(c, m, HttpStatus.CONFLICT); }
    private void log(String evento, IncubacaoAves i, Integer qtd, long inicio) { Map<String, Object> dados = new LinkedHashMap<>(); dados.put("event.action", evento); dados.put("module", "criacoes"); dados.put("criacao.aves.incubacao.id", String.valueOf(i.getId())); dados.put("event.duration", System.nanoTime() - inicio); if (qtd != null) dados.put("criacao.quantidade", qtd); try (MdcScope ignored = MdcScope.with(dados)) { log.info("Operação de incubação concluída."); } }
}
