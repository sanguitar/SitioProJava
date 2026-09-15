package com.example.sitiopro.agricultura.service;

import com.example.sitiopro.agricultura.dto.*;
import com.example.sitiopro.agricultura.entity.*;
import com.example.sitiopro.agricultura.repository.*;
import com.example.sitiopro.propriedade.entity.*;
import com.example.sitiopro.propriedade.repository.TalhaoRepository;
import com.example.sitiopro.propriedade.service.PerimetroService;
import com.example.sitiopro.propriedade.service.PropriedadeService;
import com.example.sitiopro.integracao.embrapa.agrofit.repository.AgrofitCulturaRepository;
import com.example.sitiopro.integracao.embrapa.agrofit.entity.AgrofitCultura;
import com.example.sitiopro.integracao.clima.dto.ClimaResumo;
import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalService;
import com.example.sitiopro.estoque.dto.MovimentoEstoqueRequest;
import com.example.sitiopro.estoque.entity.MovimentoEstoque;
import com.example.sitiopro.estoque.service.EstoqueMovimentoService;
import com.example.sitiopro.tarefas.dto.*;
import com.example.sitiopro.tarefas.entity.*;
import com.example.sitiopro.tarefas.service.TarefaService;
import com.example.sitiopro.tarefas.service.AlertaService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class AgriculturaService {
    private static final List<StatusCultivo> ATIVOS = List.of(StatusCultivo.PLANEJADO, StatusCultivo.IMPLANTADO,
            StatusCultivo.EM_DESENVOLVIMENTO, StatusCultivo.PRONTO_COLHEITA);
    private static final List<StatusOcorrenciaCultivo> OCORRENCIAS_ABERTAS = List.of(
            StatusOcorrenciaCultivo.ABERTA, StatusOcorrenciaCultivo.EM_ACOMPANHAMENTO);
    private static final List<SeveridadeOcorrencia> SEVERIDADES_RELEVANTES = List.of(
            SeveridadeOcorrencia.ALTA, SeveridadeOcorrencia.CRITICA);
    private final SafraRepository safras;
    private final CulturaAgricolaRepository culturas;
    private final CultivoRepository cultivos;
    private final PlantioRepository plantios;
    private final AcompanhamentoCultivoRepository acompanhamentos;
    private final ColheitaRepository colheitas;
    private final AdubacaoCultivoRepository adubacoes;
    private final IrrigacaoCultivoRepository irrigacoes;
    private final TratamentoAgricolaRepository tratamentos;
    private final OcorrenciaCultivoRepository ocorrencias;
    private final HistoricoOcorrenciaCultivoRepository historicosOcorrencia;
    private final TalhaoRepository talhoes;
    private final AgrofitCulturaRepository agrofit;
    private final PerimetroService perimetros;
    private final PropriedadeService propriedades;
    private final EstoqueMovimentoService estoque;
    private final TarefaService tarefas;
    private final AlertaService alertas;
    private final ConfiguracaoOperacionalService configuracoes;
    private final EntityManager em;
    private final Validator validator;
    private final Clock clock;

    public AgriculturaService(SafraRepository safras, CulturaAgricolaRepository culturas, CultivoRepository cultivos,
            PlantioRepository plantios, AcompanhamentoCultivoRepository acompanhamentos, ColheitaRepository colheitas,
            AdubacaoCultivoRepository adubacoes, IrrigacaoCultivoRepository irrigacoes,
            TratamentoAgricolaRepository tratamentos, OcorrenciaCultivoRepository ocorrencias,
            HistoricoOcorrenciaCultivoRepository historicosOcorrencia,
            TalhaoRepository talhoes, AgrofitCulturaRepository agrofit, PerimetroService perimetros,
            PropriedadeService propriedades,
            EstoqueMovimentoService estoque, TarefaService tarefas, AlertaService alertas,
            ConfiguracaoOperacionalService configuracoes,
            EntityManager em, Validator validator, Clock clock) {
        this.safras = safras; this.culturas = culturas; this.cultivos = cultivos;
        this.plantios = plantios; this.acompanhamentos = acompanhamentos; this.colheitas = colheitas;
        this.adubacoes = adubacoes; this.irrigacoes = irrigacoes;
        this.tratamentos = tratamentos; this.ocorrencias = ocorrencias;
        this.historicosOcorrencia = historicosOcorrencia;
        this.talhoes = talhoes; this.agrofit = agrofit; this.perimetros = perimetros; this.propriedades = propriedades;
        this.estoque = estoque; this.tarefas = tarefas; this.alertas = alertas; this.configuracoes = configuracoes;
        this.em = em; this.validator = validator; this.clock = clock;
    }

    public PaginaResponse<SafraResumo> listarSafras(int pagina) {
        return PaginaResponse.de(safras.findByPropriedadeIdOrderByDataInicioDescIdDesc(principal().getId(), pagina(pagina)).map(this::resumo));
    }
    public PaginaResponse<CulturaResumo> listarCulturas(int pagina) {
        return PaginaResponse.de(culturas.findAllByOrderByNomeComum(pagina(pagina)).map(this::resumo));
    }
    public PaginaResponse<CultivoResumo> listarCultivos(int pagina) {
        return PaginaResponse.de(cultivos.findByPropriedadeIdOrderByIdDesc(principal().getId(), pagina(pagina)).map(this::resumo));
    }
    public PaginaResponse<ColheitaResumo> listarColheitas(int pagina) {
        return PaginaResponse.de(colheitas.findByCultivoPropriedadeIdOrderByDataDescIdDesc(principal().getId(), pagina(pagina)).map(this::resumo));
    }
    public PaginaResponse<AdubacaoResumo> listarAdubacoes(int pagina) {
        return PaginaResponse.de(adubacoes.findByCultivoPropriedadeIdOrderByDataDescIdDesc(principal().getId(), pagina(pagina)).map(this::resumo));
    }
    public PaginaResponse<IrrigacaoResumo> listarIrrigacoes(int pagina) {
        return PaginaResponse.de(irrigacoes.findByCultivoPropriedadeIdOrderByDataHoraDescIdDesc(principal().getId(), pagina(pagina)).map(this::resumo));
    }
    public PaginaResponse<TratamentoResumo> listarTratamentos(int pagina) {
        return PaginaResponse.de(tratamentos.findByCultivoPropriedadeIdOrderByDataDescIdDesc(principal().getId(), pagina(pagina)).map(this::resumo));
    }
    public PaginaResponse<OcorrenciaResumo> listarOcorrencias(int pagina) {
        return PaginaResponse.de(ocorrencias.findByCultivoPropriedadeIdOrderByDataHoraDescIdDesc(principal().getId(), pagina(pagina)).map(this::resumo));
    }
    public SafraResumo detalharSafra(Long id) { return resumo(safra(id)); }
    public CulturaResumo detalharCultura(Long id) { return resumo(cultura(id)); }
    public CultivoResumo detalharCultivo(Long id) { return resumo(cultivo(id)); }
    public OcorrenciaDetalhe detalharOcorrencia(Long id) {
        OcorrenciaCultivo o = ocorrencia(id);
        return detalhe(o);
    }

    public List<OpcaoAgricola> opcoesSafras() {
        return safras.findByPropriedadeIdOrderByDataInicioDescIdDesc(principal().getId()).stream()
                .filter(s -> !s.getStatus().finalizada()).map(s -> new OpcaoAgricola(s.getId(), s.getNome())).toList();
    }
    public List<OpcaoAgricola> opcoesCulturas() {
        return culturas.findAllByOrderByNomeComum().stream().filter(CulturaAgricola::isAtivo)
                .map(c -> new OpcaoAgricola(c.getId(), c.getNomeComum())).toList();
    }
    public List<OpcaoAgricola> opcoesTalhoes() {
        return talhoes.findByPropriedadeIdOrderByNomeAsc(principal().getId()).stream()
                .filter(t -> t.getStatus() == StatusDivisaoFisica.ATIVO)
                .map(t -> new OpcaoAgricola(t.getId(), t.getCodigo() + " - " + t.getNome() + " (" + t.getAreaHa() + " ha)")).toList();
    }
    public List<OpcaoAgricola> opcoesAgrofit() {
        return agrofit.findAllByOrderByNome().stream().map(c -> new OpcaoAgricola(c.getId(), c.getNome())).toList();
    }

    public SafraRequest formularioSafra(Long id) {
        Safra s = safra(id); SafraRequest r = new SafraRequest();
        r.setNome(s.getNome()); r.setAnoInicio(s.getAnoInicio()); r.setAnoFim(s.getAnoFim());
        r.setDataInicio(s.getDataInicio()); r.setDataFim(s.getDataFim()); r.setStatus(s.getStatus());
        r.setObservacao(s.getObservacao()); r.setVersao(s.getVersao()); return r;
    }
    public CulturaRequest formularioCultura(Long id) {
        CulturaAgricola c = cultura(id); CulturaRequest r = new CulturaRequest();
        r.setNomeComum(c.getNomeComum()); r.setNomeCientifico(c.getNomeCientifico());
        r.setCicloDiasEstimado(c.getCicloDiasEstimado()); r.setAtivo(c.isAtivo());
        r.setAgrofitCulturaId(c.getAgrofitCultura() == null ? null : c.getAgrofitCultura().getId());
        r.setObservacao(c.getObservacao()); r.setVersao(c.getVersao()); return r;
    }
    public CultivoRequest formularioCultivo(Long id) {
        Cultivo c = cultivo(id); CultivoRequest r = new CultivoRequest();
        r.setSafraId(c.getSafra().getId()); r.setTalhaoId(c.getTalhao().getId()); r.setCulturaId(c.getCultura().getId());
        r.setAreaCultivadaHa(c.getAreaCultivadaHa()); r.setDataPlantio(c.getDataPlantio());
        r.setPrevisaoColheita(c.getPrevisaoColheita()); r.setObservacao(c.getObservacao()); r.setVersao(c.getVersao()); return r;
    }
    public OcorrenciaAtualizacaoRequest formularioOcorrencia(Long id) {
        OcorrenciaCultivo o = ocorrencia(id);
        OcorrenciaAtualizacaoRequest r = new OcorrenciaAtualizacaoRequest();
        r.setTipo(o.getTipo()); r.setSeveridade(o.getSeveridade()); r.setTitulo(o.getTitulo());
        r.setDescricao(o.getDescricao()); r.setAreaAfetadaHa(o.getAreaAfetadaHa());
        r.setObservacao(o.getObservacao());
        r.setAgrofitCulturaIds(o.getReferenciasAgrofit().stream().map(AgrofitCultura::getId).toList());
        r.setVersao(o.getVersao());
        return r;
    }
    public EncerramentoOcorrenciaRequest novoEncerramentoOcorrencia(long versao) {
        EncerramentoOcorrenciaRequest r = new EncerramentoOcorrenciaRequest();
        r.setDataHora(agora()); r.setVersao(versao); return r;
    }

    @Transactional
    public SafraResumo salvarSafra(Long id, SafraRequest r) {
        validar(r); Propriedade p = bloquearPropriedade();
        Safra s = id == null ? new Safra() : safra(id);
        if (id != null) versao(s.getVersao(), r.getVersao());
        if (r.getAnoFim() < r.getAnoInicio() || r.getDataInicio().getYear() < r.getAnoInicio()
                || r.getDataInicio().getYear() > r.getAnoFim()
                || (r.getDataFim() != null && (r.getDataFim().isBefore(r.getDataInicio())
                || r.getDataFim().getYear() > r.getAnoFim()))) {
            throw erro("Datas e anos da safra devem formar um intervalo valido.");
        }
        if (safras.existsByPropriedadeIdAndNomeIgnoreCaseAndIdNot(p.getId(), r.getNome().trim(), id == null ? -1L : id)) {
            throw erro("Ja existe uma safra com esse nome.");
        }
        if (id != null && safrasEmUso(s)) {
            if (r.getStatus().finalizada()) throw erro("Finalize os cultivos antes de encerrar ou cancelar a safra.");
        }
        if (id != null && cultivosDaSafraForaDoPeriodo(s, r)) {
            throw erro("O novo periodo nao pode excluir datas de cultivos ja registrados.");
        }
        s.setPropriedade(p); s.setNome(r.getNome().trim()); s.setAnoInicio(r.getAnoInicio()); s.setAnoFim(r.getAnoFim());
        s.setDataInicio(r.getDataInicio()); s.setDataFim(r.getDataFim()); s.setStatus(r.getStatus());
        s.setObservacao(texto(r.getObservacao()));
        return resumo(safras.saveAndFlush(s));
    }

    private boolean safrasEmUso(Safra s) { return cultivos.existsBySafraIdAndStatusIn(s.getId(), ATIVOS); }

    private boolean cultivosDaSafraForaDoPeriodo(Safra s, SafraRequest r) {
        LocalDate fim = r.getDataFim() == null ? LocalDate.of(r.getAnoFim(), 12, 31) : r.getDataFim();
        return cultivos.existsForaDoPeriodo(s.getId(), r.getDataInicio(), fim);
    }

    @Transactional
    public CulturaResumo salvarCultura(Long id, CulturaRequest r) {
        validar(r); bloquearPropriedade();
        CulturaAgricola c = id == null ? new CulturaAgricola() : cultura(id);
        if (id != null) versao(c.getVersao(), r.getVersao());
        if (culturas.existsByNomeComumIgnoreCaseAndIdNot(r.getNomeComum().trim(), id == null ? -1L : id)) {
            throw erro("Ja existe uma cultura com esse nome.");
        }
        c.setNomeComum(r.getNomeComum().trim()); c.setNomeCientifico(texto(r.getNomeCientifico()));
        c.setCicloDiasEstimado(r.getCicloDiasEstimado()); c.setAtivo(r.isAtivo());
        c.setAgrofitCultura(r.getAgrofitCulturaId() == null ? null : agrofit.findById(r.getAgrofitCulturaId())
                .orElseThrow(() -> erro("Referencia Agrofit nao encontrada no catalogo local.")));
        c.setObservacao(texto(r.getObservacao()));
        return resumo(culturas.saveAndFlush(c));
    }

    @Transactional
    public CultivoResumo salvarCultivo(Long id, CultivoRequest r) {
        validar(r); Propriedade p = bloquearPropriedade();
        Cultivo c = id == null ? new Cultivo() : cultivo(id);
        if (id != null) { versao(c.getVersao(), r.getVersao()); aberto(c); }
        Safra s = safra(r.getSafraId());
        if (s.getStatus().finalizada()) throw erro("Selecione uma safra aberta.");
        Talhao t = talhoes.findByIdAndPropriedadeId(r.getTalhaoId(), p.getId())
                .orElseThrow(() -> erro("Selecione um Talhao oficial desta propriedade."));
        CulturaAgricola cultura = cultura(r.getCulturaId());
        if (t.getStatus() != StatusDivisaoFisica.ATIVO || !cultura.isAtivo()) {
            throw erro("Talhao e cultura precisam estar ativos.");
        }
        if (id != null && plantios.existsByCultivoId(id) && (!Objects.equals(c.getSafra().getId(), s.getId())
                || !Objects.equals(c.getTalhao().getId(), t.getId()) || !Objects.equals(c.getCultura().getId(), cultura.getId())
                || c.getAreaCultivadaHa().compareTo(r.getAreaCultivadaHa()) != 0 || !c.getDataPlantio().equals(r.getDataPlantio()))) {
            throw erro("Apos o plantio, preserve safra, Talhao, cultura, area e data do plantio.");
        }
        validarDataSafra(r.getDataPlantio(), s);
        BigDecimal reservada = cultivos.areaReservada(t.getId(), ATIVOS, id == null ? -1L : id);
        if (r.getAreaCultivadaHa().add(reservada).compareTo(t.getAreaHa()) > 0) {
            throw erro("A area solicitada, somada aos cultivos abertos, excede a area do Talhao.");
        }
        LocalDate previsao = r.getPrevisaoColheita() == null
                ? previsao(r.getDataPlantio(), cultura.getCicloDiasEstimado()) : r.getPrevisaoColheita();
        if (previsao != null && previsao.isBefore(r.getDataPlantio())) throw erro("Previsao de colheita anterior ao plantio.");
        c.setPropriedade(p); c.setSafra(s); c.setTalhao(t); c.setCultura(cultura);
        c.setAreaCultivadaHa(r.getAreaCultivadaHa()); c.setDataPlantio(r.getDataPlantio());
        c.setPrevisaoColheita(previsao); c.setObservacao(texto(r.getObservacao()));
        return resumo(cultivos.saveAndFlush(c));
    }

    @Transactional
    public CultivoResumo alterarStatus(Long id, StatusCultivoRequest r) {
        validar(r); Cultivo c = operacao(id, r.versao());
        StatusCultivo destino = r.status();
        boolean permitido = destino == StatusCultivo.CANCELADO || destino == StatusCultivo.PERDIDO
                || (c.getStatus() == StatusCultivo.IMPLANTADO && destino == StatusCultivo.EM_DESENVOLVIMENTO)
                || ((c.getStatus() == StatusCultivo.IMPLANTADO || c.getStatus() == StatusCultivo.EM_DESENVOLVIMENTO)
                    && destino == StatusCultivo.PRONTO_COLHEITA);
        if (!permitido) throw erro("Transicao invalida. Implantacao e colheita exigem o registro da operacao.");
        c.setStatus(destino); em.flush(); return resumo(c);
    }

    @Transactional
    public PlantioResumo registrarPlantio(Long id, PlantioRequest r) {
        validar(r); Cultivo c = operacao(id, r.getVersao());
        validarDataSafra(r.getData(), c.getSafra());
        if (r.getData().isAfter(hoje())) throw erro("Plantio realizado nao pode ter data futura.");
        if (c.getTalhao().getStatus() != StatusDivisaoFisica.ATIVO) throw erro("Talhao inativo para novo plantio.");
        if (c.getStatus() == StatusCultivo.PRONTO_COLHEITA || colheitas.findFirstByCultivoIdOrderByDataDescIdDesc(id).isPresent()) {
            throw erro("Nao registre plantio depois de iniciar a colheita.");
        }
        boolean primeiro = !plantios.existsByCultivoId(id);
        if (!primeiro && r.getData().isBefore(c.getDataPlantio())) throw erro("Data anterior ao primeiro plantio.");
        Plantio p = new Plantio(); p.setCultivo(c); p.setData(r.getData()); p.setMetodo(texto(r.getMetodo()));
        p.setQuantidade(r.getQuantidade()); p.setUnidade(r.getUnidade().trim()); p.setEspacamento(texto(r.getEspacamento()));
        p.setOrigem(r.getOrigem()); p.setDescricaoOrigem(texto(r.getDescricaoOrigem())); p.setObservacao(texto(r.getObservacao()));
        if (r.getOrigem() == OrigemPlantio.EXTERNA) {
            if (texto(r.getDescricaoOrigem()) == null) throw erro("Descreva a origem externa das sementes ou mudas.");
            if (r.getItemEstoqueId() != null || r.getLocalEstoqueId() != null || texto(r.getLoteCodigo()) != null) {
                throw erro("Origem externa nao deve informar item, local ou lote de Estoque.");
            }
        } else {
            if (r.getItemEstoqueId() == null || r.getLocalEstoqueId() == null) throw erro("Informe item e local do Estoque.");
            var item = estoque.detalharItem(r.getItemEstoqueId()).resumo();
            if (!item.unidade().equalsIgnoreCase(r.getUnidade().trim())) throw erro("Use a unidade do item de Estoque: " + item.unidade() + ".");
            MovimentoEstoqueRequest movimento = new MovimentoEstoqueRequest();
            movimento.setItemId(r.getItemEstoqueId()); movimento.setLocalOrigemId(r.getLocalEstoqueId());
            movimento.setQuantidade(r.getQuantidade()); movimento.setLoteCodigo(texto(r.getLoteCodigo()));
            movimento.setDataMovimento(r.getData().atStartOfDay());
            movimento.setObservacao("Plantio do cultivo #" + id);
            p.setMovimentoEstoque(estoque.registrarConsumoAgricultura(movimento, id));
            p.setUnidade(item.unidade()); p.setDescricaoOrigem(item.nome());
        }
        if (primeiro) {
            boolean automatica = Objects.equals(c.getPrevisaoColheita(), previsao(c.getDataPlantio(), c.getCultura().getCicloDiasEstimado()));
            c.setDataPlantio(r.getData());
            if (automatica) c.setPrevisaoColheita(previsao(r.getData(), c.getCultura().getCicloDiasEstimado()));
            if (c.getPrevisaoColheita() != null && c.getPrevisaoColheita().isBefore(r.getData())) throw erro("Revise a previsao antes de registrar este plantio.");
            c.setStatus(StatusCultivo.IMPLANTADO);
        }
        return resumo(plantios.saveAndFlush(p));
    }

    @Transactional
    public AcompanhamentoResumo registrarAcompanhamento(Long id, AcompanhamentoRequest r) {
        validar(r); Cultivo c = operacao(id, r.getVersao());
        if (r.getDataHora().isAfter(agora())) throw erro("Acompanhamento nao pode ter data futura.");
        validarDataSafra(r.getDataHora().toLocalDate(), c.getSafra());
        if (c.getStatus() != StatusCultivo.PLANEJADO && r.getDataHora().toLocalDate().isBefore(c.getDataPlantio())) {
            throw erro("Acompanhamento anterior ao plantio.");
        }
        AcompanhamentoCultivo a = new AcompanhamentoCultivo(); a.setCultivo(c);
        a.setDataHora(r.getDataHora()); a.setTipo(r.getTipo()); a.setDescricao(r.getDescricao().trim());
        a.setObservacao(texto(r.getObservacao()));
        return resumo(acompanhamentos.saveAndFlush(a));
    }

    @Transactional
    public ColheitaResumo registrarColheita(Long id, ColheitaRequest r) {
        validar(r); bloquearPropriedade(); Cultivo c = cultivo(id);
        String chave = texto(r.getChaveIdempotencia());
        if (chave != null) {
            var existente = colheitas.findByCultivoIdAndChaveIdempotencia(id, chave);
            if (existente.isPresent()) return resumo(existente.get());
        }
        prepararOperacao(c, r.getVersao());
        if (!plantios.existsByCultivoId(id)) throw erro("Registre o plantio antes da colheita.");
        if (plantios.existsByCultivoIdAndDataAfter(id, r.getData())) throw erro("Colheita anterior ao ultimo plantio.");
        if (r.getData().isBefore(c.getDataPlantio()) || r.getData().isAfter(hoje())) throw erro("Data de colheita invalida.");
        validarDataSafra(r.getData(), c.getSafra());
        var ultima = colheitas.findFirstByCultivoIdOrderByDataDescIdDesc(id);
        if (ultima.isPresent() && (!ultima.get().getUnidade().equalsIgnoreCase(r.getUnidade().trim())
                || r.getData().isBefore(ultima.get().getData()))) {
            throw erro("Preserve a unidade e a ordem cronologica das colheitas deste cultivo.");
        }
        Colheita h = new Colheita(); h.setCultivo(c); h.setData(r.getData()); h.setQuantidade(r.getQuantidade());
        h.setUnidade(ultima.map(Colheita::getUnidade).orElse(r.getUnidade().trim()));
        h.setClassificacao(texto(r.getClassificacao())); h.setPerdas(r.getPerdas()); h.setFinalizaCultivo(r.isFinalizaCultivo());
        h.setDestino(r.getDestino()); h.setChaveIdempotencia(chave); h.setObservacao(texto(r.getObservacao()));
        if (r.getDestino() == DestinoColheita.SEM_ESTOQUE) {
            if (r.getItemEstoqueId() != null || r.getLocalEstoqueId() != null || texto(r.getLoteCodigo()) != null
                    || r.getValidade() != null) throw erro("Destino sem Estoque nao deve informar item, local, lote ou validade.");
        } else {
            if (chave == null) throw erro("Informe a chave de idempotencia para entrada no Estoque.");
            if (r.getItemEstoqueId() == null || r.getLocalEstoqueId() == null) throw erro("Informe item e local de destino no Estoque.");
            var item = estoque.detalharItem(r.getItemEstoqueId()).resumo();
            if (!item.unidade().equalsIgnoreCase(h.getUnidade())) throw erro("Use a unidade do item de Estoque: " + item.unidade() + ".");
            MovimentoEstoqueRequest movimento = movimento(r.getItemEstoqueId(), null, r.getLocalEstoqueId(),
                    r.getQuantidade(), r.getLoteCodigo(), r.getValidade(), r.getData().atStartOfDay(),
                    "Colheita do cultivo #" + id);
            h.setMovimentoEstoque(estoque.registrarEntradaAgriculturaColheita(movimento, id));
            h.setUnidade(item.unidade());
        }
        if (r.isFinalizaCultivo()) { c.setStatus(StatusCultivo.COLHIDO); c.setDataColheitaReal(r.getData()); }
        else c.setStatus(StatusCultivo.PRONTO_COLHEITA);
        return resumo(colheitas.saveAndFlush(h));
    }

    @Transactional
    public AdubacaoResumo registrarAdubacao(Long id, AdubacaoRequest r) {
        validar(r); bloquearPropriedade(); Cultivo c = cultivo(id);
        var existente = adubacoes.findByCultivoIdAndChaveIdempotencia(id, r.getChaveIdempotencia().trim());
        if (existente.isPresent()) return resumo(existente.get());
        prepararOperacao(c, r.getVersao()); validarOperacaoCampo(c, r.getData()); validarArea(r.getAreaAplicadaHa(), c);
        AdubacaoCultivo a = new AdubacaoCultivo(); a.setCultivo(c); a.setData(r.getData());
        a.setProduto(r.getProduto().trim()); a.setQuantidade(r.getQuantidade()); a.setUnidade(r.getUnidade().trim());
        a.setAreaAplicadaHa(r.getAreaAplicadaHa()); a.setMetodo(texto(r.getMetodo())); a.setOrigem(r.getOrigem());
        aplicarInsumo(id, "Adubacao", r.getOrigem(), r.getDescricaoOrigem(), r.getProduto(), r.getItemEstoqueId(),
                r.getLocalEstoqueId(), r.getLoteCodigo(), r.getQuantidade(), r.getUnidade(), r.getData(),
                (produto, unidade, descricao, movimento) -> {
                    a.setProduto(produto); a.setUnidade(unidade); a.setDescricaoOrigem(descricao); a.setMovimentoEstoque(movimento);
                });
        a.setChaveIdempotencia(r.getChaveIdempotencia().trim()); a.setObservacao(texto(r.getObservacao()));
        return resumo(adubacoes.saveAndFlush(a));
    }

    @Transactional
    public IrrigacaoResumo registrarIrrigacao(Long id, IrrigacaoRequest r) {
        validar(r); bloquearPropriedade(); Cultivo c = cultivo(id);
        var existente = irrigacoes.findByCultivoIdAndChaveIdempotencia(id, r.getChaveIdempotencia().trim());
        if (existente.isPresent()) return resumo(existente.get());
        prepararOperacao(c, r.getVersao()); validarOperacaoCampo(c, r.getDataHora().toLocalDate());
        if (r.getDataHora().isAfter(agora())) throw erro("Irrigacao nao pode ter data futura.");
        if (r.getDuracaoMinutos() == null && r.getVolumeLitros() == null) {
            throw erro("Informe a duracao ou o volume da irrigacao.");
        }
        IrrigacaoCultivo i = new IrrigacaoCultivo(); i.setCultivo(c); i.setDataHora(r.getDataHora());
        i.setDuracaoMinutos(r.getDuracaoMinutos()); i.setVolumeLitros(r.getVolumeLitros());
        i.setMetodo(texto(r.getMetodo())); i.setChaveIdempotencia(r.getChaveIdempotencia().trim());
        i.setObservacao(texto(r.getObservacao())); return resumo(irrigacoes.saveAndFlush(i));
    }

    @Transactional
    public TratamentoResumo registrarTratamento(Long id, TratamentoRequest r) {
        validar(r); bloquearPropriedade(); Cultivo c = cultivo(id);
        var existente = tratamentos.findByCultivoIdAndChaveIdempotencia(id, r.getChaveIdempotencia().trim());
        if (existente.isPresent()) return resumo(existente.get());
        prepararOperacao(c, r.getVersao()); validarOperacaoCampo(c, r.getData()); validarArea(r.getAreaTratadaHa(), c);
        TratamentoAgricola t = new TratamentoAgricola(); t.setCultivo(c); t.setData(r.getData());
        t.setFinalidade(r.getFinalidade().trim()); t.setProdutoAplicado(r.getProdutoAplicado().trim());
        t.setQuantidade(r.getQuantidade()); t.setUnidade(r.getUnidade().trim()); t.setAreaTratadaHa(r.getAreaTratadaHa());
        t.setMetodo(texto(r.getMetodo())); t.setOrigem(r.getOrigem());
        aplicarInsumo(id, "Tratamento agricola", r.getOrigem(), r.getDescricaoOrigem(), r.getProdutoAplicado(), r.getItemEstoqueId(),
                r.getLocalEstoqueId(), r.getLoteCodigo(), r.getQuantidade(), r.getUnidade(), r.getData(),
                (produto, unidade, descricao, movimento) -> {
                    t.setProdutoAplicado(produto); t.setUnidade(unidade); t.setDescricaoOrigem(descricao); t.setMovimentoEstoque(movimento);
                });
        t.setChaveIdempotencia(r.getChaveIdempotencia().trim()); t.setObservacao(texto(r.getObservacao()));
        return resumo(tratamentos.saveAndFlush(t));
    }

    @Transactional
    public OcorrenciaResumo registrarOcorrencia(Long id, OcorrenciaRequest r) {
        validar(r); bloquearPropriedade(); Cultivo c = cultivo(id);
        var existente = ocorrencias.findByCultivoIdAndChaveIdempotencia(id, r.getChaveIdempotencia().trim());
        if (existente.isPresent()) return resumo(existente.get());
        prepararOperacao(c, r.getVersao()); validarOperacaoCampo(c, r.getDataHora().toLocalDate());
        if (r.getDataHora().isAfter(agora())) throw erro("Ocorrencia nao pode ter data futura.");
        validarArea(r.getAreaAfetadaHa(), c);
        if ((r.getQuantidadePerdida() == null) != (texto(r.getUnidadePerda()) == null)) {
            throw erro("Quantidade perdida e unidade devem ser informadas juntas.");
        }
        OcorrenciaCultivo o = new OcorrenciaCultivo(); o.setCultivo(c); o.setDataHora(r.getDataHora());
        o.setTipo(r.getTipo()); o.setSeveridade(r.getSeveridade()); o.setTitulo(r.getTitulo().trim());
        o.setDescricao(r.getDescricao().trim());
        o.setAreaAfetadaHa(r.getAreaAfetadaHa()); o.setQuantidadePerdida(r.getQuantidadePerdida());
        o.setUnidadePerda(texto(r.getUnidadePerda())); o.setPerdaTotal(r.isPerdaTotal());
        o.setChaveIdempotencia(r.getChaveIdempotencia().trim()); o.setObservacao(texto(r.getObservacao()));
        o.substituirReferenciasAgrofit(referenciasAgrofit(r.getAgrofitCulturaIds()));
        if (r.isPerdaTotal()) c.setStatus(StatusCultivo.PERDIDO);
        o = ocorrencias.saveAndFlush(o);
        registrarHistorico(o, r.getDataHora(), TipoHistoricoOcorrencia.REGISTRO,
                "Ocorrencia registrada: " + o.getTitulo(), r.getChaveIdempotencia());
        sincronizarAlertasOcorrencias();
        return resumo(o);
    }

    @Transactional
    public OcorrenciaResumo atualizarOcorrencia(Long id, OcorrenciaAtualizacaoRequest r) {
        validar(r); bloquearPropriedade();
        OcorrenciaCultivo o = ocorrenciaParaAtualizacao(id);
        String chave = r.getChaveIdempotencia().trim();
        if (historicosOcorrencia.findByOcorrenciaIdAndChaveIdempotencia(id, chave).isPresent()) {
            return resumo(o);
        }
        versao(o.getVersao(), r.getVersao());
        if (!o.getStatus().aberta()) throw conflito("Ocorrencia encerrada; atualizacao indisponivel.");
        validarArea(r.getAreaAfetadaHa(), o.getCultivo());
        o.setTipo(r.getTipo()); o.setSeveridade(r.getSeveridade()); o.setTitulo(r.getTitulo().trim());
        o.setDescricao(r.getDescricao().trim()); o.setAreaAfetadaHa(r.getAreaAfetadaHa());
        o.setObservacao(texto(r.getObservacao())); o.setStatus(StatusOcorrenciaCultivo.EM_ACOMPANHAMENTO);
        o.substituirReferenciasAgrofit(referenciasAgrofit(r.getAgrofitCulturaIds()));
        o.getCultivo().setRevisaoOperacoes(o.getCultivo().getRevisaoOperacoes() + 1);
        o = ocorrencias.saveAndFlush(o);
        registrarHistorico(o, agora(), TipoHistoricoOcorrencia.ATUALIZACAO,
                r.getAcompanhamento().trim(), chave);
        sincronizarAlertasOcorrencias();
        return resumo(o);
    }

    @Transactional
    public OcorrenciaResumo encerrarOcorrencia(Long id, EncerramentoOcorrenciaRequest r) {
        validar(r); bloquearPropriedade();
        OcorrenciaCultivo o = ocorrenciaParaAtualizacao(id);
        String chave = r.getChaveIdempotencia().trim();
        if (historicosOcorrencia.findByOcorrenciaIdAndChaveIdempotencia(id, chave).isPresent()) {
            return resumo(o);
        }
        versao(o.getVersao(), r.getVersao());
        if (!o.getStatus().aberta()) throw conflito("Ocorrencia ja encerrada.");
        if (r.getDataHora().isBefore(o.getDataHora()) || r.getDataHora().isAfter(agora())) {
            throw erro("Data de encerramento invalida.");
        }
        o.setStatus(StatusOcorrenciaCultivo.ENCERRADA); o.setEncerradaEm(r.getDataHora());
        o.setResolucao(r.getResolucao().trim());
        o.getCultivo().setRevisaoOperacoes(o.getCultivo().getRevisaoOperacoes() + 1);
        o = ocorrencias.saveAndFlush(o);
        registrarHistorico(o, r.getDataHora(), TipoHistoricoOcorrencia.ENCERRAMENTO,
                r.getResolucao().trim(), chave);
        sincronizarAlertasOcorrencias();
        return resumo(o);
    }

    @Transactional
    public TarefaResumo criarTarefaInspecao(Long id, UsuarioAtor ator) {
        bloquearPropriedade(); OcorrenciaCultivo o = ocorrenciaParaAtualizacao(id);
        if (!o.getStatus().aberta()) throw conflito("Ocorrencia encerrada; tarefa de inspecao indisponivel.");
        PrioridadeTarefa prioridade = switch (o.getSeveridade()) {
            case CRITICA -> PrioridadeTarefa.CRITICA;
            case ALTA -> PrioridadeTarefa.ALTA;
            default -> PrioridadeTarefa.NORMAL;
        };
        return tarefas.sincronizarAutomatica(new TarefaAutomaticaRequest(
                "AGRICULTURA:OCORRENCIA:" + id + ":INSPECAO",
                "Inspecionar ocorrencia: " + o.getTitulo(),
                "Verificar em campo e registrar o acompanhamento da ocorrencia #" + id + ".",
                prioridade, agora().plusDays(1), ModuloOrigem.AGRICULTURA, referenciaOcorrencia(id)), ator);
    }

    @Transactional
    public TarefaDetalhe criarTarefa(Long id, TarefaRequest r, UsuarioAtor ator) {
        validar(r); bloquearPropriedade(); Cultivo c = cultivo(id); aberto(c);
        return tarefas.criarVinculada(r, ator, ModuloOrigem.AGRICULTURA, referencia(id));
    }

    public CultivoDetalhe detalheLocal(Long id) {
        Cultivo c = cultivo(id);
        return new CultivoDetalhe(resumo(c),
                plantios.findByCultivoIdOrderByDataDescIdDesc(id).stream().map(this::resumo).toList(),
                acompanhamentos.findByCultivoIdOrderByDataHoraDescIdDesc(id).stream().map(this::resumo).toList(),
                colheitas.findByCultivoIdOrderByDataDescIdDesc(id).stream().map(this::resumo).toList(),
                adubacoes.findByCultivoIdOrderByDataDescIdDesc(id).stream().map(this::resumo).toList(),
                irrigacoes.findByCultivoIdOrderByDataHoraDescIdDesc(id).stream().map(this::resumo).toList(),
                tratamentos.findByCultivoIdOrderByDataDescIdDesc(id).stream().map(this::resumo).toList(),
                ocorrencias.findByCultivoIdOrderByDataHoraDescIdDesc(id).stream().map(this::resumo).toList(),
                tarefas.listarRelacionadas(ModuloOrigem.AGRICULTURA, referencia(id)), ClimaResumo.naoSincronizado());
    }

    public AgriculturaResumo painel() {
        Long propriedadeId = principal().getId(); LocalDate hoje = hoje();
        var avisos = cultivos.findByPropriedadeIdAndStatusInAndPrevisaoColheitaLessThanEqualOrderByPrevisaoColheitaAscIdAsc(
                propriedadeId, ATIVOS, hoje.plusDays(7), PageRequest.of(0, 10)).stream()
                .map(c -> new AvisoAgricola(c.getId(), c.getCultura().getNomeComum(),
                        c.getPrevisaoColheita().isBefore(hoje) ? "Previsao de colheita vencida" : "Colheita prevista nos proximos 7 dias")).toList();
        LocalDateTime agora = agora();
        var trabalhos = cultivos.proximosTrabalhos(propriedadeId, ModuloOrigem.AGRICULTURA,
                List.of(StatusTarefa.PENDENTE, StatusTarefa.EM_ANDAMENTO), PageRequest.of(0, 8)).stream()
                .map(t -> new TarefaResumo(t.getId(), t.getTitulo(), t.getStatus(), t.getPrioridade(), t.getDataVencimento(),
                        t.getResponsavel() == null ? null : t.getResponsavel().getId(),
                        t.getResponsavel() == null ? null : t.getResponsavel().getNome(), t.getOrigem(), t.getModuloOrigem(),
                        t.getDataVencimento() != null && t.getDataVencimento().isBefore(agora))).toList();
        return new AgriculturaResumo(safras.findFirstByPropriedadeIdAndStatusOrderByDataInicioDescIdDesc(
                propriedadeId, StatusSafra.EM_ANDAMENTO).map(this::resumo).orElse(null),
                cultivos.countByPropriedadeIdAndStatusIn(propriedadeId, ATIVOS), cultivos.areaAtiva(propriedadeId, ATIVOS),
                ocorrencias.countByCultivoPropriedadeIdAndStatusIn(propriedadeId, OCORRENCIAS_ABERTAS),
                ocorrencias.countByCultivoPropriedadeIdAndStatusInAndSeveridadeIn(
                        propriedadeId, OCORRENCIAS_ABERTAS, SEVERIDADES_RELEVANTES),
                ocorrencias.contarCultivosAfetados(propriedadeId, OCORRENCIAS_ABERTAS),
                trabalhos, avisos, colheitas.findByCultivoPropriedadeIdOrderByDataDescIdDesc(propriedadeId, PageRequest.of(0, 8))
                .stream().map(this::resumo).toList());
    }

    public MapaOperacionalAgriculturaResumo mapaOperacional() {
        Long propriedadeId = principal().getId();
        var perimetro = perimetros.obter();
        Map<Long, OcorrenciasTalhao> ocorrenciasPorTalhao = ocorrenciasAbertasPorTalhao(propriedadeId);
        Map<Long, CultivoMapaResumo> cultivosPorTalhao = new LinkedHashMap<>();
        for (Cultivo cultivo : cultivos.findByPropriedadeIdAndStatusInOrderByDataPlantioDescIdDesc(propriedadeId, ATIVOS)) {
            Long talhaoId = cultivo.getTalhao().getId();
            if (!cultivosPorTalhao.containsKey(talhaoId)) {
                OcorrenciasTalhao resumo = ocorrenciasPorTalhao.getOrDefault(talhaoId, OcorrenciasTalhao.vazio());
                cultivosPorTalhao.put(talhaoId, new CultivoMapaResumo(cultivo.getId(),
                cultivo.getCultura().getNomeComum(), cultivo.getSafra().getNome(), cultivo.getAreaCultivadaHa(),
                cultivo.getDataPlantio(), cultivo.getPrevisaoColheita(), cultivo.getStatus(),
                resumo.quantidade(), resumo.severidadeMaisAlta()));
            }
        }
        var talhoesMapa = perimetro.talhoes().isEmpty() ? propriedades.mapaTalhoes() : perimetro.talhoes();
        var talhoesOperacionais = talhoesMapa.stream()
                .map(t -> TalhaoOperacionalMapaResumo.de(t, cultivosPorTalhao.get(t.id())))
                .toList();
        return MapaOperacionalAgriculturaResumo.de(perimetro.getMapa(), talhoesOperacionais);
    }

    private Map<Long, OcorrenciasTalhao> ocorrenciasAbertasPorTalhao(Long propriedadeId) {
        Map<Long, OcorrenciasTalhao> resultado = new HashMap<>();
        for (OcorrenciaCultivo ocorrencia : ocorrencias.findByCultivoPropriedadeIdAndStatusIn(
                propriedadeId, OCORRENCIAS_ABERTAS)) {
            Long talhaoId = ocorrencia.getCultivo().getTalhao().getId();
            resultado.merge(talhaoId, OcorrenciasTalhao.de(ocorrencia.getSeveridade()), OcorrenciasTalhao::somar);
        }
        return resultado;
    }

    private OcorrenciaDetalhe detalhe(OcorrenciaCultivo o) {
        return new OcorrenciaDetalhe(resumo(o),
                historicosOcorrencia.findByOcorrenciaIdOrderByDataHoraDescIdDesc(o.getId()).stream()
                        .map(this::resumo).toList(),
                tarefas.listarRelacionadas(ModuloOrigem.AGRICULTURA, referenciaOcorrencia(o.getId())));
    }

    private void registrarHistorico(OcorrenciaCultivo ocorrencia, LocalDateTime dataHora,
            TipoHistoricoOcorrencia tipo, String descricao, String chave) {
        HistoricoOcorrenciaCultivo h = new HistoricoOcorrenciaCultivo();
        h.setOcorrencia(ocorrencia); h.setDataHora(dataHora); h.setTipo(tipo);
        h.setSeveridade(ocorrencia.getSeveridade()); h.setStatus(ocorrencia.getStatus());
        h.setDescricao(descricao); h.setChaveIdempotencia(chave.trim());
        historicosOcorrencia.saveAndFlush(h);
    }

    private List<AgrofitCultura> referenciasAgrofit(List<Long> ids) {
        List<Long> unicos = ids == null ? List.of() : ids.stream().filter(Objects::nonNull).distinct().toList();
        if (unicos.isEmpty()) return List.of();
        List<AgrofitCultura> referencias = agrofit.findAllById(unicos);
        if (referencias.size() != unicos.size()) throw erro("Uma referencia Agrofit local nao foi encontrada.");
        return referencias;
    }

    private void sincronizarAlertasOcorrencias() {
        List<CondicaoAlerta> condicoes = ocorrencias.findByStatusInAndSeveridadeInOrderById(
                        OCORRENCIAS_ABERTAS, SEVERIDADES_RELEVANTES).stream()
                .map(o -> new CondicaoAlerta(
                        "AGRICULTURA:OCORRENCIA:" + o.getId() + ":FITOSSANITARIA",
                        o.getSeveridade().getRotulo() + ": " + o.getTitulo(),
                        o.getDescricao(),
                        o.getSeveridade() == SeveridadeOcorrencia.CRITICA
                                ? SeveridadeAlerta.CRITICA : SeveridadeAlerta.ALTA,
                        referenciaOcorrencia(o.getId()),
                        Map.of("ocorrenciaId", o.getId(), "cultivoId", o.getCultivo().getId())))
                .toList();
        alertas.sincronizar(ModuloOrigem.AGRICULTURA,
                TipoAlerta.AGRICULTURA_OCORRENCIA_FITOSSANITARIA, condicoes);
    }

    private Propriedade principal() { return propriedades.principal(); }
    private Propriedade bloquearPropriedade() {
        Propriedade p = principal();
        // Serialize reservations and season closure until commit without changing the physical registry.
        em.refresh(p, LockModeType.PESSIMISTIC_WRITE);
        if (!p.isAtivo()) throw erro("A propriedade esta inativa.");
        return p;
    }
    private Cultivo operacao(Long id, Long versao) {
        bloquearPropriedade(); Cultivo c = cultivo(id); prepararOperacao(c, versao);
        return c;
    }
    private void prepararOperacao(Cultivo c, Long versao) {
        versao(c.getVersao(), versao); aberto(c);
        if (c.getSafra().getStatus().finalizada()) throw erro("A safra esta finalizada.");
        c.setRevisaoOperacoes(c.getRevisaoOperacoes() + 1);
    }
    private void validarOperacaoCampo(Cultivo c, LocalDate data) {
        if (!plantios.existsByCultivoId(c.getId())) throw erro("Registre o plantio antes da operacao de campo.");
        if (data.isBefore(c.getDataPlantio()) || data.isAfter(hoje())) throw erro("Data da operacao de campo invalida.");
        validarDataSafra(data, c.getSafra());
    }
    private void validarArea(BigDecimal area, Cultivo c) {
        if (area != null && area.compareTo(c.getAreaCultivadaHa()) > 0) {
            throw erro("A area informada excede a area cultivada.");
        }
    }
    private void aplicarInsumo(Long cultivoId, String operacao, OrigemInsumo origem, String descricaoOrigem,
            String produto, Long itemId, Long localId, String loteCodigo, BigDecimal quantidade, String unidade, LocalDate data,
            AplicadorInsumo aplicador) {
        if (origem == OrigemInsumo.EXTERNA) {
            String descricao = texto(descricaoOrigem);
            if (descricao == null) throw erro("Descreva a origem externa do insumo.");
            if (itemId != null || localId != null || texto(loteCodigo) != null) {
                throw erro("Origem externa nao deve informar item, local ou lote de Estoque.");
            }
            aplicador.aplicar(produto.trim(), unidade.trim(), descricao, null);
            return;
        }
        if (itemId == null || localId == null) throw erro("Informe item e local de origem no Estoque.");
        var item = estoque.detalharItem(itemId).resumo();
        if (!item.unidade().equalsIgnoreCase(unidade.trim())) throw erro("Use a unidade do item de Estoque: " + item.unidade() + ".");
        MovimentoEstoqueRequest movimento = movimento(itemId, localId, null, quantidade, loteCodigo, null,
                data.atStartOfDay(), operacao + " do cultivo #" + cultivoId);
        MovimentoEstoque salvo = estoque.registrarConsumoAgriculturaOperacao(movimento, cultivoId, operacao);
        aplicador.aplicar(item.nome(), item.unidade(), item.nome(), salvo);
    }
    private MovimentoEstoqueRequest movimento(Long itemId, Long origemId, Long destinoId, BigDecimal quantidade,
            String loteCodigo, LocalDate validade, LocalDateTime data, String observacao) {
        MovimentoEstoqueRequest m = new MovimentoEstoqueRequest(); m.setItemId(itemId); m.setLocalOrigemId(origemId);
        m.setLocalDestinoId(destinoId); m.setQuantidade(quantidade); m.setLoteCodigo(texto(loteCodigo));
        m.setValidade(validade); m.setDataMovimento(data); m.setObservacao(observacao); return m;
    }
    @FunctionalInterface
    private interface AplicadorInsumo {
        void aplicar(String produto, String unidade, String descricaoOrigem, MovimentoEstoque movimento);
    }
    private void aberto(Cultivo c) { if (c.getStatus().finalizado()) throw conflito("Cultivo finalizado; operacao indisponivel."); }
    private Safra safra(Long id) { return safras.findByIdAndPropriedadeId(id, principal().getId()).orElseThrow(this::ausente); }
    private CulturaAgricola cultura(Long id) { return culturas.findById(id).orElseThrow(this::ausente); }
    private Cultivo cultivo(Long id) { return cultivos.findByIdAndPropriedadeId(id, principal().getId()).orElseThrow(this::ausente); }
    private OcorrenciaCultivo ocorrencia(Long id) {
        return ocorrencias.findByIdAndCultivoPropriedadeId(id, principal().getId()).orElseThrow(this::ausente);
    }
    private OcorrenciaCultivo ocorrenciaParaAtualizacao(Long id) {
        return ocorrencias.buscarParaAtualizacao(id, principal().getId()).orElseThrow(this::ausente);
    }
    private void versao(long atual, Long recebida) {
        if (recebida == null || atual != recebida) throw conflito("Registro alterado. Recarregue a pagina antes de tentar novamente.");
    }
    private void validarDataSafra(LocalDate data, Safra s) {
        if (data.isBefore(s.getDataInicio()) || data.getYear() > s.getAnoFim()
                || (s.getDataFim() != null && data.isAfter(s.getDataFim()))) throw erro("Data fora do periodo da safra.");
    }
    private LocalDate previsao(LocalDate plantio, Integer dias) { return dias == null ? null : plantio.plusDays(dias); }
    private LocalDate hoje() { return agora().toLocalDate(); }
    private LocalDateTime agora() { return LocalDateTime.ofInstant(clock.instant(), configuracoes.obter().zoneId()); }
    private String referencia(Long id) { return "CULTIVO:" + id; }
    private String referenciaOcorrencia(Long id) { return "OCORRENCIA:" + id; }
    private String texto(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
    private PageRequest pagina(int n) { return PageRequest.of(Math.max(0, n), 20); }
    private <T> void validar(T r) {
        var erros = validator.validate(r); if (!erros.isEmpty()) throw new ConstraintViolationException(erros);
    }
    private AgriculturaOperacaoException erro(String m) { return new AgriculturaOperacaoException(m); }
    private AgriculturaOperacaoException conflito(String m) { return new AgriculturaOperacaoException(m, HttpStatus.CONFLICT); }
    private AgriculturaOperacaoException ausente() { return new AgriculturaOperacaoException("Registro agricola nao encontrado.", HttpStatus.NOT_FOUND); }

    private SafraResumo resumo(Safra s) {
        return new SafraResumo(s.getId(), s.getPropriedade().getId(), s.getNome(), s.getAnoInicio(), s.getAnoFim(),
                s.getDataInicio(), s.getDataFim(), s.getStatus(), s.getObservacao(), s.getVersao());
    }
    private CulturaResumo resumo(CulturaAgricola c) {
        return new CulturaResumo(c.getId(), c.getNomeComum(), c.getNomeCientifico(), c.getCicloDiasEstimado(), c.isAtivo(),
                c.getAgrofitCultura() == null ? null : c.getAgrofitCultura().getId(),
                c.getAgrofitCultura() == null ? null : c.getAgrofitCultura().getNome(), c.getObservacao(), c.getVersao());
    }
    private CultivoResumo resumo(Cultivo c) {
        return new CultivoResumo(c.getId(), c.getSafra().getId(), c.getSafra().getNome(),
                c.getTalhao().getId(), c.getTalhao().getCodigo(), c.getTalhao().getNome(), c.getCultura().getId(),
                c.getCultura().getNomeComum(), c.getAreaCultivadaHa(), c.getDataPlantio(),
                c.getStatus() == StatusCultivo.PLANEJADO ? null : Math.max(0, ChronoUnit.DAYS.between(c.getDataPlantio(),
                c.getDataColheitaReal() == null ? hoje() : c.getDataColheitaReal())),
                c.getPrevisaoColheita(), c.getDataColheitaReal(), c.getStatus(), c.getObservacao(), c.getVersao());
    }
    private PlantioResumo resumo(Plantio p) {
        return new PlantioResumo(p.getId(), p.getData(), p.getMetodo(), p.getQuantidade(), p.getUnidade(), p.getEspacamento(),
                p.getOrigem(), p.getDescricaoOrigem(), p.getMovimentoEstoque() == null ? null : p.getMovimentoEstoque().getId(),
                p.getObservacao(), p.getCriadoPor());
    }
    private AcompanhamentoResumo resumo(AcompanhamentoCultivo a) {
        return new AcompanhamentoResumo(a.getId(), a.getDataHora(), a.getTipo(), a.getDescricao(), a.getObservacao(), a.getCriadoPor());
    }
    private ColheitaResumo resumo(Colheita c) {
        return new ColheitaResumo(c.getId(), c.getCultivo().getId(), c.getCultivo().getCultura().getNomeComum(),
                c.getData(), c.getQuantidade(), c.getUnidade(), c.getClassificacao(), c.getPerdas(), c.isFinalizaCultivo(),
                c.getDestino(), c.getMovimentoEstoque() == null ? null : c.getMovimentoEstoque().getId(),
                c.getObservacao(), c.getCriadoPor());
    }
    private AdubacaoResumo resumo(AdubacaoCultivo a) {
        return new AdubacaoResumo(a.getId(), a.getCultivo().getId(), a.getCultivo().getCultura().getNomeComum(),
                a.getData(), a.getProduto(), a.getQuantidade(), a.getUnidade(), a.getAreaAplicadaHa(), a.getMetodo(),
                a.getOrigem(), a.getDescricaoOrigem(), a.getMovimentoEstoque() == null ? null : a.getMovimentoEstoque().getId(),
                a.getObservacao(), a.getCriadoPor());
    }
    private IrrigacaoResumo resumo(IrrigacaoCultivo i) {
        return new IrrigacaoResumo(i.getId(), i.getCultivo().getId(), i.getCultivo().getCultura().getNomeComum(),
                i.getDataHora(), i.getDuracaoMinutos(), i.getVolumeLitros(), i.getMetodo(), i.getObservacao(), i.getCriadoPor());
    }
    private TratamentoResumo resumo(TratamentoAgricola t) {
        return new TratamentoResumo(t.getId(), t.getCultivo().getId(), t.getCultivo().getCultura().getNomeComum(),
                t.getData(), t.getFinalidade(), t.getProdutoAplicado(), t.getQuantidade(), t.getUnidade(),
                t.getAreaTratadaHa(), t.getMetodo(), t.getOrigem(), t.getDescricaoOrigem(),
                t.getMovimentoEstoque() == null ? null : t.getMovimentoEstoque().getId(), t.getObservacao(), t.getCriadoPor());
    }
    private OcorrenciaResumo resumo(OcorrenciaCultivo o) {
        return new OcorrenciaResumo(o.getId(), o.getCultivo().getId(), o.getCultivo().getCultura().getNomeComum(),
                o.getDataHora(), o.getTipo(), o.getTitulo(), o.getDescricao(), o.getSeveridade(), o.getAreaAfetadaHa(),
                o.getQuantidadePerdida(), o.getUnidadePerda(), o.isPerdaTotal(), o.getObservacao(), o.getStatus(),
                o.getResolucao(), o.getEncerradaEm(), o.getReferenciasAgrofit().stream()
                        .sorted(Comparator.comparing(AgrofitCultura::getNome))
                        .map(a -> new ReferenciaAgrofitResumo(a.getId(), a.getNome(), a.getObtidoEm())).toList(),
                o.getCriadoPor(), o.getVersao());
    }
    private OcorrenciaHistoricoResumo resumo(HistoricoOcorrenciaCultivo h) {
        return new OcorrenciaHistoricoResumo(h.getId(), h.getDataHora(), h.getTipo(), h.getSeveridade(),
                h.getStatus(), h.getDescricao(), h.getCriadoPor());
    }

    private record OcorrenciasTalhao(long quantidade, SeveridadeOcorrencia severidadeMaisAlta) {
        static OcorrenciasTalhao vazio() {
            return new OcorrenciasTalhao(0, null);
        }
        static OcorrenciasTalhao de(SeveridadeOcorrencia severidade) {
            return new OcorrenciasTalhao(1, severidade);
        }
        OcorrenciasTalhao somar(OcorrenciasTalhao outra) {
            return new OcorrenciasTalhao(quantidade + outra.quantidade,
                    severidadeMaisAlta(severidadeMaisAlta, outra.severidadeMaisAlta));
        }
        private static SeveridadeOcorrencia severidadeMaisAlta(SeveridadeOcorrencia atual,
                SeveridadeOcorrencia candidata) {
            if (atual == null) return candidata;
            if (candidata == null) return atual;
            return rank(candidata) > rank(atual) ? candidata : atual;
        }
        private static int rank(SeveridadeOcorrencia severidade) {
            return switch (severidade) {
                case BAIXA -> 1;
                case MEDIA -> 2;
                case ALTA -> 3;
                case CRITICA -> 4;
            };
        }
    }
}
