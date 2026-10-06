package com.example.sitiopro.criacao.peixes.service;

import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.criacao.peixes.dto.*;
import com.example.sitiopro.criacao.peixes.entity.*;
import com.example.sitiopro.criacao.peixes.repository.*;
import com.example.sitiopro.estoque.dto.MovimentoEstoqueRequest;
import com.example.sitiopro.estoque.entity.*;
import com.example.sitiopro.estoque.service.*;
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
public class PeixesSanidadeService {
    private final RegistroSanitarioPeixesRepository registros;
    private final LotePeixesRepository lotes;
    private final EstoqueCatalogoService catalogo;
    private final EstoqueMovimentoService estoque;
    private final CodigoCriacaoService codigos;
    private final TarefaService tarefas;
    private final PeixesSanidadeAlertasService alertas;
    private final Clock clock;

    public PeixesSanidadeService(RegistroSanitarioPeixesRepository registros,
            LotePeixesRepository lotes, EstoqueCatalogoService catalogo,
            EstoqueMovimentoService estoque, CodigoCriacaoService codigos,
            TarefaService tarefas, PeixesSanidadeAlertasService alertas, Clock clock) {
        this.registros = registros;
        this.lotes = lotes;
        this.catalogo = catalogo;
        this.estoque = estoque;
        this.codigos = codigos;
        this.tarefas = tarefas;
        this.alertas = alertas;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<RegistroSanitarioPeixesResumo> listar(Long loteId) {
        List<RegistroSanitarioPeixes> resultado = loteId == null
                ? registros.findAllByOrderByDataProcedimentoDescIdDesc()
                : registros.findByLoteIdOrderByDataProcedimentoDescIdDesc(loteId);
        return resultado.stream().map(this::resumo).toList();
    }

    @Transactional(readOnly = true)
    public RegistroSanitarioPeixesResumo detalhar(Long id) { return resumo(buscar(id)); }

    @Transactional(readOnly = true)
    public SanidadePeixesResumo resumoOperacional() {
        LocalDateTime agora = LocalDateTime.now(clock);
        return new SanidadePeixesResumo(
                registros.countByDataProcedimentoGreaterThanEqual(agora.minusDays(30)),
                registros.countByProximaAcaoConcluidaFalseAndProximaAcaoDataGreaterThanEqual(agora),
                registros.countByProximaAcaoConcluidaFalseAndProximaAcaoDataBefore(agora));
    }

    @Transactional
    public RegistroSanitarioPeixesResumo registrar(RegistroSanitarioPeixesRequest request, UsuarioAtor ator) {
        String chave = obrigatorio(request.getChaveIdempotencia(), "Chave de idempotência");
        codigos.bloquearIdempotencia("PEIXES_SANIDADE", chave);
        RegistroSanitarioPeixes existente = registros.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) {
            if (!Objects.equals(existente.getLote().getId(), request.getLoteId()))
                throw erro("CHAVE_IDEMPOTENCIA_DIVERGENTE", "A chave já pertence a outro lote.");
            return resumo(existente);
        }
        validar(request);

        LotePeixes lote = lotes.findById(request.getLoteId()).orElseThrow(() ->
                new PeixesOperacaoException("LOTE_NAO_ENCONTRADO", "Lote de peixes não encontrado.",
                        HttpStatus.NOT_FOUND));
        RegistroSanitarioPeixes registro = new RegistroSanitarioPeixes();
        registro.setLote(lote);
        registro.setTipo(request.getTipo());
        registro.setDataProcedimento(request.getDataProcedimento());
        registro.setProcedimentoProduto(obrigatorio(request.getProcedimentoProduto(), "Procedimento/produto"));
        registro.setMotivo(texto(request.getMotivo()));
        registro.setResponsavel(obrigatorio(request.getResponsavel(), "Responsável"));
        registro.setObservacao(texto(request.getObservacao()));
        registro.setProximaAcao(texto(request.getProximaAcao()));
        registro.setProximaAcaoData(request.getProximaAcaoData());
        registro.setCusto(dinheiro(request.getCusto()));
        registro.setChaveIdempotencia(chave);

        ItemEstoque item = request.getItemEstoqueId() == null
                ? null : catalogo.buscarItem(request.getItemEstoqueId());
        registro.setItemEstoque(item);
        registro.setLoteEstoqueCodigo(texto(request.getLoteEstoqueCodigo()));
        registro = registros.saveAndFlush(registro);

        if (request.getQuantidadeConsumida() != null) {
            LocalEstoque local = catalogo.buscarLocalAtivo(request.getLocalEstoqueId());
            BigDecimal quantidade = positivo(request.getQuantidadeConsumida(), "Quantidade consumida");
            registro.setLocalEstoque(local);
            registro.setQuantidadeConsumida(quantidade);
            MovimentoEstoqueRequest movimento = new MovimentoEstoqueRequest();
            movimento.setItemId(item.getId());
            movimento.setQuantidade(quantidade);
            movimento.setLocalOrigemId(local.getId());
            movimento.setLoteCodigo(registro.getLoteEstoqueCodigo());
            movimento.setObservacao(registro.getObservacao());
            movimento.setDataMovimento(request.getDataProcedimento());
            registro.setMovimentoEstoque(estoque.registrarConsumoSanidadePeixes(
                    movimento, registro.getId(), lote.getId()));
        }
        sincronizarTarefa(registro, ator);
        registros.flush();
        alertas.avaliar();
        return resumo(registro);
    }

    @Transactional
    public RegistroSanitarioPeixesResumo concluirProximaAcao(Long id, UsuarioAtor ator) {
        RegistroSanitarioPeixes registro = registros.buscarParaAtualizacao(id)
                .orElseThrow(() -> naoEncontrado(id));
        if (registro.getProximaAcaoData() == null)
            throw erro("PROXIMA_ACAO_INEXISTENTE", "O registro não possui próxima ação.");
        if (!registro.isProximaAcaoConcluida()) {
            registro.setProximaAcaoConcluida(true);
            registro.setProximaAcaoConcluidaEm(LocalDateTime.now(clock));
            tarefas.concluirAutomatica(chaveTarefa(id), ator);
            registros.flush();
            alertas.avaliar();
        }
        return resumo(registro);
    }

    private void sincronizarTarefa(RegistroSanitarioPeixes registro, UsuarioAtor ator) {
        if (registro.getProximaAcaoData() == null) return;
        tarefas.sincronizarAutomatica(new TarefaAutomaticaRequest(chaveTarefa(registro.getId()),
                registro.getProximaAcao(),
                "Acompanhamento sanitário do lote " + registro.getLote().getCodigo() + ".",
                PrioridadeTarefa.ALTA, registro.getProximaAcaoData(), ModuloOrigem.CRIACOES,
                PeixesService.referencia(registro.getLote().getId())), ator);
    }

    private void validar(RegistroSanitarioPeixesRequest request) {
        if (request.getLoteId() == null) throw erro("LOTE_OBRIGATORIO", "Informe o lote de peixes.");
        if (request.getTipo() == null) throw erro("TIPO_OBRIGATORIO", "Informe o tipo de registro.");
        LocalDateTime agora = LocalDateTime.now(clock);
        if (request.getDataProcedimento() == null || request.getDataProcedimento().isAfter(agora.plusMinutes(1)))
            throw erro("DATA_PROCEDIMENTO_INVALIDA", "A data do procedimento é obrigatória e não pode estar no futuro.");
        boolean possuiAcao = StringUtils.hasText(request.getProximaAcao());
        if (possuiAcao != (request.getProximaAcaoData() != null))
            throw erro("PROXIMA_ACAO_INVALIDA", "Informe a próxima ação e sua data em conjunto.");
        if (request.getProximaAcaoData() != null
                && request.getProximaAcaoData().isBefore(request.getDataProcedimento()))
            throw erro("DATA_PROXIMA_ACAO_INVALIDA", "A próxima ação não pode preceder o procedimento.");
        if (request.getCusto() != null && request.getCusto().signum() < 0)
            throw erro("CUSTO_INVALIDO", "O custo não pode ser negativo.");
        if (request.getQuantidadeConsumida() != null
                && (request.getItemEstoqueId() == null || request.getLocalEstoqueId() == null))
            throw erro("CONSUMO_INCOMPLETO", "Para consumir estoque, informe item, local e quantidade.");
        if (request.getQuantidadeConsumida() == null && request.getLocalEstoqueId() != null)
            throw erro("CONSUMO_INCOMPLETO", "Informe a quantidade consumida para o local selecionado.");
    }

    private RegistroSanitarioPeixes buscar(Long id) {
        return registros.findById(id).orElseThrow(() -> naoEncontrado(id));
    }
    private RegistroSanitarioPeixesResumo resumo(RegistroSanitarioPeixes r) {
        return new RegistroSanitarioPeixesResumo(r.getId(), r.getLote().getId(), r.getLote().getCodigo(),
                r.getTipo(), r.getDataProcedimento(), r.getProcedimentoProduto(), r.getMotivo(),
                r.getResponsavel(), r.getObservacao(), r.getProximaAcao(), r.getProximaAcaoData(),
                r.isProximaAcaoConcluida(), r.getProximaAcaoConcluidaEm(), r.getCusto(),
                r.getItemEstoque() == null ? null : r.getItemEstoque().getId(),
                r.getItemEstoque() == null ? null : r.getItemEstoque().getNome(),
                r.getLocalEstoque() == null ? null : r.getLocalEstoque().getId(),
                r.getLocalEstoque() == null ? null : r.getLocalEstoque().getNome(),
                r.getQuantidadeConsumida(), r.getLoteEstoqueCodigo(),
                r.getMovimentoEstoque() == null ? null : r.getMovimentoEstoque().getId(),
                r.getVersao(), r.getCriadoEm(), r.getCriadoPor());
    }
    public static String chaveTarefa(Long id) { return "CRIACAO:PEIXES:SANIDADE:" + id + ":PROXIMA_ACAO"; }
    private String obrigatorio(String v, String campo) { if (!StringUtils.hasText(v)) throw erro("CAMPO_OBRIGATORIO", campo + " é obrigatório."); return v.trim(); }
    private String texto(String v) { return StringUtils.hasText(v) ? v.trim() : null; }
    private BigDecimal positivo(BigDecimal v, String campo) { if (v == null || v.signum() <= 0) throw erro("VALOR_INVALIDO", campo + " deve ser maior que zero."); return v.setScale(4, RoundingMode.HALF_UP); }
    private BigDecimal dinheiro(BigDecimal v) { return v == null ? null : v.setScale(2, RoundingMode.HALF_UP); }
    private PeixesOperacaoException naoEncontrado(Long id) { return new PeixesOperacaoException("REGISTRO_SANITARIO_NAO_ENCONTRADO", "Registro sanitário não encontrado: " + id, HttpStatus.NOT_FOUND); }
    private PeixesOperacaoException erro(String c, String m) { return new PeixesOperacaoException(c, m); }
}
