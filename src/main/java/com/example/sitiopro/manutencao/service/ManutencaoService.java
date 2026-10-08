package com.example.sitiopro.manutencao.service;

import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.estoque.dto.MovimentoEstoqueRequest;
import com.example.sitiopro.estoque.entity.*;
import com.example.sitiopro.estoque.service.*;
import com.example.sitiopro.manutencao.dto.*;
import com.example.sitiopro.manutencao.entity.*;
import com.example.sitiopro.manutencao.repository.*;
import com.example.sitiopro.propriedade.entity.EstruturaPropriedade;
import com.example.sitiopro.propriedade.repository.EstruturaPropriedadeRepository;
import com.example.sitiopro.propriedade.repository.PropriedadeRepository;
import com.example.sitiopro.tarefas.dto.TarefaAutomaticaRequest;
import com.example.sitiopro.tarefas.entity.*;
import com.example.sitiopro.tarefas.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.*;
import java.time.*;
import java.util.*;

@Service
public class ManutencaoService {
    private final AtivoPatrimonialRepository ativos;
    private final RegistroManutencaoRepository registros;
    private final EstruturaPropriedadeRepository estruturas;
    private final PropriedadeRepository propriedades;
    private final EstoqueCatalogoService catalogo;
    private final EstoqueMovimentoService estoque;
    private final CodigoCriacaoService codigos;
    private final TarefaService tarefas;
    private final ManutencaoAlertasService alertas;
    private final ManutencaoPreventivaService preventiva;
    private final Clock clock;

    public ManutencaoService(AtivoPatrimonialRepository ativos, RegistroManutencaoRepository registros,
            EstruturaPropriedadeRepository estruturas, PropriedadeRepository propriedades,
            EstoqueCatalogoService catalogo, EstoqueMovimentoService estoque,
            CodigoCriacaoService codigos, TarefaService tarefas,
            ManutencaoAlertasService alertas, ManutencaoPreventivaService preventiva, Clock clock) {
        this.ativos = ativos; this.registros = registros; this.estruturas = estruturas;
        this.propriedades = propriedades; this.catalogo = catalogo; this.estoque = estoque;
        this.codigos = codigos; this.tarefas = tarefas; this.alertas = alertas; this.preventiva = preventiva; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<AtivoPatrimonialResumo> listarAtivos() {
        return ativos.findAllByOrderByNomeAsc().stream().map(this::resumo).toList();
    }

    @Transactional(readOnly = true)
    public AtivoPatrimonialResumo detalharAtivo(Long id) { return resumo(buscarAtivo(id)); }

    @Transactional(readOnly = true)
    public AtivoPatrimonialRequest formularioAtivo(Long id) {
        AtivoPatrimonial a = buscarAtivo(id);
        AtivoPatrimonialRequest r = new AtivoPatrimonialRequest();
        r.setNome(a.getNome()); r.setTipo(a.getTipo()); r.setMarca(a.getMarca()); r.setModelo(a.getModelo());
        r.setNumeroSerie(a.getNumeroSerie()); r.setDataAquisicao(a.getDataAquisicao());
        r.setValorAquisicao(a.getValorAquisicao()); r.setLocalizacao(a.getLocalizacao());
        r.setEstruturaId(a.getEstrutura() == null ? null : a.getEstrutura().getId());
        r.setStatus(a.getStatus()); r.setObservacao(a.getObservacao());
        r.setChaveIdempotencia(a.getChaveIdempotencia()); r.setVersao(a.getVersao());
        return r;
    }

    @Transactional
    public AtivoPatrimonialResumo criarAtivo(AtivoPatrimonialRequest request) {
        validarAtivo(request);
        String chave = obrigatorio(request.getChaveIdempotencia(), "Chave de idempotência");
        codigos.bloquearIdempotencia("PATRIMONIO_ATIVO", chave);
        AtivoPatrimonial existente = ativos.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) return resumo(existente);
        AtivoPatrimonial ativo = new AtivoPatrimonial();
        ativo.setCodigo(codigos.proximoAtivoPatrimonial());
        ativo.setChaveIdempotencia(chave);
        preencher(ativo, request);
        return resumo(ativos.saveAndFlush(ativo));
    }

    @Transactional
    public AtivoPatrimonialResumo atualizarAtivo(Long id, AtivoPatrimonialRequest request) {
        validarAtivo(request);
        AtivoPatrimonial ativo = ativos.buscarParaAtualizacao(id).orElseThrow(() -> ativoAusente(id));
        if (request.getVersao() == null || request.getVersao() != ativo.getVersao())
            throw erro("VERSAO_DESATUALIZADA", "O ativo foi alterado por outro usuário. Recarregue a página.", HttpStatus.CONFLICT);
        preencher(ativo, request);
        ativos.flush();
        return resumo(ativo);
    }

    @Transactional(readOnly = true)
    public List<RegistroManutencaoResumo> listarManutencoes(Long ativoId) {
        return registros.findByAtivoIdOrderByDataManutencaoDescIdDesc(ativoId).stream().map(this::resumo).toList();
    }

    @Transactional(readOnly = true)
    public RegistroManutencaoResumo detalharManutencao(Long id) {
        return resumo(registros.findById(id).orElseThrow(() -> manutencaoAusente(id)));
    }

    @Transactional(readOnly = true)
    public ManutencaoDashboardResumo dashboard() {
        LocalDateTime agora = LocalDateTime.now(clock);
        List<PlanoManutencaoPreventivaResumo> planos = preventiva.planosPrioritarios();
        return new ManutencaoDashboardResumo(
                ativos.countByStatusIn(List.of(StatusAtivoPatrimonial.ATIVO, StatusAtivoPatrimonial.EM_MANUTENCAO)),
                ativos.countByStatus(StatusAtivoPatrimonial.EM_MANUTENCAO),
                registros.countByProximaManutencaoConcluidaFalseAndProximaManutencaoBefore(agora),
                registros.countByProximaManutencaoConcluidaFalseAndProximaManutencaoBetween(agora, agora.plusDays(30)),
                preventiva.contarPlanosAtivos(), preventiva.contarPlanosVencidos(), preventiva.contarPlanosProximos(),
                registros.somarCustosDesde(agora.minusDays(30)),
                planos,
                registros.findTop10ByOrderByDataManutencaoDescIdDesc().stream().map(this::resumo).toList());
    }

    @Transactional
    public RegistroManutencaoResumo registrarManutencao(RegistroManutencaoRequest request, UsuarioAtor ator) {
        validarManutencao(request);
        String chave = obrigatorio(request.getChaveIdempotencia(), "Chave de idempotência");
        codigos.bloquearIdempotencia("MANUTENCAO_REGISTRO", chave);
        RegistroManutencao existente = registros.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) {
            if (!Objects.equals(existente.getAtivo().getId(), request.getAtivoId()))
                throw erro("CHAVE_IDEMPOTENCIA_DIVERGENTE", "A chave já pertence a outro ativo.");
            return resumo(existente);
        }
        AtivoPatrimonial ativo = buscarAtivo(request.getAtivoId());
        PlanoManutencaoPreventiva plano = preventiva.buscarPlanoAtivoDoAtivo(request.getPlanoPreventivoId(), ativo.getId());
        RegistroManutencao registro = new RegistroManutencao();
        registro.setAtivo(ativo); registro.setPlanoPreventivo(plano); registro.setTipo(request.getTipo());
        registro.setDataManutencao(request.getDataManutencao());
        registro.setDescricao(obrigatorio(request.getDescricao(), "Descrição"));
        registro.setResponsavel(obrigatorio(request.getResponsavel(), "Responsável"));
        registro.setHorimetro(decimal(request.getHorimetro(), 2));
        registro.setQuilometragem(decimal(request.getQuilometragem(), 2));
        registro.setCusto(decimal(request.getCusto(), 2));
        registro.setProximaManutencao(request.getProximaManutencao());
        registro.setObservacao(texto(request.getObservacao()));
        registro.setChaveIdempotencia(chave);
        registro = registros.saveAndFlush(registro);

        for (ConsumoManutencaoRequest consumoRequest : consumosInformados(request)) {
            ItemEstoque item = catalogo.buscarItem(consumoRequest.getItemEstoqueId());
            LocalEstoque local = catalogo.buscarLocalAtivo(consumoRequest.getLocalEstoqueId());
            BigDecimal quantidade = positivo(consumoRequest.getQuantidade(), "Quantidade consumida");
            MovimentoEstoqueRequest movimentoRequest = new MovimentoEstoqueRequest();
            movimentoRequest.setItemId(item.getId()); movimentoRequest.setQuantidade(quantidade);
            movimentoRequest.setLocalOrigemId(local.getId());
            movimentoRequest.setLoteCodigo(texto(consumoRequest.getLoteEstoqueCodigo()));
            movimentoRequest.setObservacao("Manutenção " + registro.getTipo().getRotulo() + " do ativo " + ativo.getCodigo());
            movimentoRequest.setDataMovimento(registro.getDataManutencao());
            MovimentoEstoque movimento = estoque.registrarConsumoManutencao(movimentoRequest, registro.getId(), ativo.getId());
            ConsumoManutencao consumo = new ConsumoManutencao();
            consumo.setItem(item); consumo.setLocal(local); consumo.setQuantidade(quantidade);
            consumo.setLoteEstoqueCodigo(texto(consumoRequest.getLoteEstoqueCodigo()));
            consumo.setMovimentoEstoque(movimento); registro.adicionarConsumo(consumo);
        }
        preventiva.concluirCiclo(request.getPlanoPreventivoId(), ativo.getId(), registro.getDataManutencao(),
                registro.getHorimetro(), registro.getQuilometragem(), ator);
        sincronizarTarefa(registro, ator);
        registros.flush();
        alertas.avaliar();
        return resumo(registro);
    }

    @Transactional
    public RegistroManutencaoResumo concluirProximaManutencao(Long id, UsuarioAtor ator) {
        RegistroManutencao registro = registros.buscarParaAtualizacao(id).orElseThrow(() -> manutencaoAusente(id));
        if (registro.getProximaManutencao() == null)
            throw erro("PROXIMA_MANUTENCAO_INEXISTENTE", "O registro não possui próxima manutenção.");
        if (!registro.isProximaManutencaoConcluida()) {
            registro.setProximaManutencaoConcluida(true);
            registro.setProximaManutencaoConcluidaEm(LocalDateTime.now(clock));
            tarefas.concluirAutomatica(chaveTarefa(id), ator);
            registros.flush(); alertas.avaliar();
        }
        return resumo(registro);
    }

    private void preencher(AtivoPatrimonial ativo, AtivoPatrimonialRequest r) {
        ativo.setNome(obrigatorio(r.getNome(), "Nome")); ativo.setTipo(r.getTipo());
        ativo.setMarca(texto(r.getMarca())); ativo.setModelo(texto(r.getModelo()));
        ativo.setNumeroSerie(texto(r.getNumeroSerie())); ativo.setDataAquisicao(r.getDataAquisicao());
        ativo.setValorAquisicao(decimal(r.getValorAquisicao(), 2)); ativo.setLocalizacao(texto(r.getLocalizacao()));
        ativo.setEstrutura(estrutura(r.getEstruturaId())); ativo.setStatus(r.getStatus());
        ativo.setObservacao(texto(r.getObservacao()));
    }

    private EstruturaPropriedade estrutura(Long id) {
        if (id == null) return null;
        Long propriedadeId = propriedades.findByPrincipalTrue().orElseThrow(() -> erro("PROPRIEDADE_AUSENTE", "Propriedade principal não configurada.")).getId();
        return estruturas.findByIdAndPropriedadeId(id, propriedadeId)
                .orElseThrow(() -> erro("ESTRUTURA_INVALIDA", "Estrutura não encontrada na propriedade principal."));
    }

    private void sincronizarTarefa(RegistroManutencao r, UsuarioAtor ator) {
        if (r.getProximaManutencao() == null) return;
        tarefas.sincronizarAutomatica(new TarefaAutomaticaRequest(chaveTarefa(r.getId()),
                "Manutenção de " + r.getAtivo().getCodigo(),
                "Próxima manutenção de " + r.getAtivo().getNome() + ".",
                PrioridadeTarefa.ALTA, r.getProximaManutencao(), ModuloOrigem.MANUTENCAO,
                referencia(r.getAtivo().getId())), ator);
    }

    private void validarAtivo(AtivoPatrimonialRequest r) {
        if (r.getTipo() == null) throw erro("TIPO_ATIVO_OBRIGATORIO", "Informe o tipo do ativo.");
        if (r.getStatus() == null) throw erro("STATUS_ATIVO_OBRIGATORIO", "Informe o status do ativo.");
        if (r.getValorAquisicao() != null && r.getValorAquisicao().signum() < 0)
            throw erro("VALOR_AQUISICAO_INVALIDO", "O valor de aquisição não pode ser negativo.");
        if (r.getDataAquisicao() != null && r.getDataAquisicao().isAfter(LocalDate.now(clock)))
            throw erro("DATA_AQUISICAO_INVALIDA", "A data de aquisição não pode estar no futuro.");
    }

    private void validarManutencao(RegistroManutencaoRequest r) {
        if (r.getAtivoId() == null) throw erro("ATIVO_OBRIGATORIO", "Informe o ativo.");
        if (r.getTipo() == null) throw erro("TIPO_MANUTENCAO_OBRIGATORIO", "Informe o tipo de manutenção.");
        if (r.getDataManutencao() == null || r.getDataManutencao().isAfter(LocalDateTime.now(clock).plusMinutes(1)))
            throw erro("DATA_MANUTENCAO_INVALIDA", "A data da manutenção é obrigatória e não pode estar no futuro.");
        if (r.getProximaManutencao() != null && r.getProximaManutencao().isBefore(r.getDataManutencao()))
            throw erro("PROXIMA_MANUTENCAO_INVALIDA", "A próxima manutenção não pode preceder a manutenção realizada.");
        if (r.getPlanoPreventivoId() != null && r.getProximaManutencao() != null)
            throw erro("AGENDAMENTO_DUPLICADO", "Uma manutenção vinculada a plano já calcula o próximo ciclo automaticamente.");
        if (r.getCusto() == null || r.getCusto().signum() < 0) throw erro("CUSTO_INVALIDO", "O custo não pode ser negativo.");
        if (r.getHorimetro() != null && r.getHorimetro().signum() < 0) throw erro("HORIMETRO_INVALIDO", "O horímetro não pode ser negativo.");
        if (r.getQuilometragem() != null && r.getQuilometragem().signum() < 0) throw erro("QUILOMETRAGEM_INVALIDA", "A quilometragem não pode ser negativa.");
        for (ConsumoManutencaoRequest c : consumosInformados(r)) {
            if (c.getItemEstoqueId() == null || c.getLocalEstoqueId() == null || c.getQuantidade() == null || c.getQuantidade().signum() <= 0)
                throw erro("CONSUMO_INCOMPLETO", "Cada consumo deve informar item, local e quantidade maior que zero.");
        }
    }

    private List<ConsumoManutencaoRequest> consumosInformados(RegistroManutencaoRequest r) {
        return r.getConsumos() == null ? List.of() : r.getConsumos().stream().filter(Objects::nonNull).filter(c -> !c.vazio()).toList();
    }
    private AtivoPatrimonial buscarAtivo(Long id) { return ativos.findById(id).orElseThrow(() -> ativoAusente(id)); }
    private AtivoPatrimonialResumo resumo(AtivoPatrimonial a) {
        return new AtivoPatrimonialResumo(a.getId(), a.getCodigo(), a.getNome(), a.getTipo(), a.getMarca(), a.getModelo(),
                a.getNumeroSerie(), a.getDataAquisicao(), a.getValorAquisicao(), a.getLocalizacao(),
                a.getEstrutura() == null ? null : a.getEstrutura().getId(), a.getEstrutura() == null ? null : a.getEstrutura().getNome(),
                a.getStatus(), a.getObservacao(), a.getVersao(), a.getCriadoEm(), a.getCriadoPor());
    }
    private RegistroManutencaoResumo resumo(RegistroManutencao r) {
        List<ConsumoManutencaoResumo> consumos = r.getConsumos().stream().map(c -> new ConsumoManutencaoResumo(
                c.getId(), c.getItem().getId(), c.getItem().getNome(), c.getLocal().getId(), c.getLocal().getNome(),
                c.getQuantidade(), c.getItem().getUnidadeMedida().getSigla(), c.getLoteEstoqueCodigo(), c.getMovimentoEstoque().getId())).toList();
        return new RegistroManutencaoResumo(r.getId(), r.getAtivo().getId(), r.getAtivo().getCodigo(), r.getAtivo().getNome(),
                r.getPlanoPreventivo() == null ? null : r.getPlanoPreventivo().getId(),
                r.getPlanoPreventivo() == null ? null : r.getPlanoPreventivo().getNome(),
                r.getTipo(), r.getDataManutencao(), r.getDescricao(), r.getResponsavel(), r.getHorimetro(), r.getQuilometragem(),
                r.getCusto(), r.getProximaManutencao(), r.isProximaManutencaoConcluida(), r.getProximaManutencaoConcluidaEm(),
                r.getObservacao(), consumos, r.getVersao(), r.getCriadoEm(), r.getCriadoPor());
    }
    public static String referencia(Long ativoId) { return "ATIVO_PATRIMONIAL:" + ativoId; }
    public static String chaveTarefa(Long registroId) { return "MANUTENCAO:REGISTRO:" + registroId + ":PROXIMA"; }
    private String obrigatorio(String v, String campo) { if (!StringUtils.hasText(v)) throw erro("CAMPO_OBRIGATORIO", campo + " é obrigatório."); return v.trim(); }
    private String texto(String v) { return StringUtils.hasText(v) ? v.trim() : null; }
    private BigDecimal decimal(BigDecimal v, int escala) { return v == null ? null : v.setScale(escala, RoundingMode.HALF_UP); }
    private BigDecimal positivo(BigDecimal v, String campo) { if (v == null || v.signum() <= 0) throw erro("VALOR_INVALIDO", campo + " deve ser maior que zero."); return decimal(v, 4); }
    private ManutencaoOperacaoException ativoAusente(Long id) { return erro("ATIVO_NAO_ENCONTRADO", "Ativo não encontrado: " + id, HttpStatus.NOT_FOUND); }
    private ManutencaoOperacaoException manutencaoAusente(Long id) { return erro("MANUTENCAO_NAO_ENCONTRADA", "Manutenção não encontrada: " + id, HttpStatus.NOT_FOUND); }
    private ManutencaoOperacaoException erro(String c, String m) { return new ManutencaoOperacaoException(c, m); }
    private ManutencaoOperacaoException erro(String c, String m, HttpStatus s) { return new ManutencaoOperacaoException(c, m, s); }
}
