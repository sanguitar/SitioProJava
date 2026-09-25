package com.example.sitiopro.criacao.suinos.service;

import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.criacao.suinos.dto.*;
import com.example.sitiopro.criacao.suinos.entity.*;
import com.example.sitiopro.criacao.suinos.repository.*;
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
public class SuinosSanidadeService {
    private final RegistroSanitarioSuinosRepository registros;
    private final LoteSuinosRepository lotes;
    private final AnimalReprodutivoSuinosRepository animais;
    private final EstoqueCatalogoService catalogo;
    private final EstoqueMovimentoService estoque;
    private final CodigoCriacaoService codigos;
    private final TarefaService tarefas;
    private final Clock clock;

    public SuinosSanidadeService(RegistroSanitarioSuinosRepository registros,
            LoteSuinosRepository lotes, AnimalReprodutivoSuinosRepository animais,
            EstoqueCatalogoService catalogo, EstoqueMovimentoService estoque,
            CodigoCriacaoService codigos, TarefaService tarefas, Clock clock) {
        this.registros = registros; this.lotes = lotes; this.animais = animais;
        this.catalogo = catalogo; this.estoque = estoque; this.codigos = codigos;
        this.tarefas = tarefas; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<RegistroSanitarioSuinosResumo> listar(Long loteId, Long animalId) {
        validarFiltro(loteId, animalId);
        List<RegistroSanitarioSuinos> resultado = loteId != null
                ? registros.findByLoteIdOrderByDataProcedimentoDescIdDesc(loteId)
                : animalId != null
                        ? registros.findByAnimalReprodutivoIdOrderByDataProcedimentoDescIdDesc(animalId)
                        : registros.findAllByOrderByDataProcedimentoDescIdDesc();
        return resultado.stream().map(this::resumo).toList();
    }

    @Transactional(readOnly = true)
    public RegistroSanitarioSuinosResumo detalhar(Long id) { return resumo(buscar(id)); }

    @Transactional(readOnly = true)
    public SanidadeSuinosResumo resumoOperacional() {
        LocalDate hoje = LocalDate.now(clock);
        return new SanidadeSuinosResumo(registros.countByDataProcedimentoGreaterThanEqual(hoje.minusDays(30)),
                registros.countByProximaAcaoConcluidaFalseAndProximaAcaoDataGreaterThanEqual(hoje),
                registros.countByProximaAcaoConcluidaFalseAndProximaAcaoDataBefore(hoje));
    }

    @Transactional
    public RegistroSanitarioSuinosResumo registrar(RegistroSanitarioSuinosRequest request, UsuarioAtor ator) {
        String chave = obrigatorio(request.getChaveIdempotencia(), "Chave de idempotência");
        codigos.bloquearIdempotencia("SUINOS_SANIDADE", chave);
        RegistroSanitarioSuinos existente = registros.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) return resumo(existente);
        validar(request);

        RegistroSanitarioSuinos registro = new RegistroSanitarioSuinos();
        definirAlvo(registro, request.getLoteId(), request.getAnimalReprodutivoId());
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

        ItemEstoque item = request.getItemEstoqueId() == null ? null : catalogo.buscarItem(request.getItemEstoqueId());
        registro.setItemEstoque(item);
        registro.setLoteEstoqueCodigo(texto(request.getLoteEstoqueCodigo()));
        registro = registros.save(registro);
        registros.flush();

        if (request.getQuantidadeConsumida() != null) {
            LocalEstoque local = catalogo.buscarLocalAtivo(request.getLocalEstoqueId());
            BigDecimal quantidade = positivo(request.getQuantidadeConsumida(), "Quantidade consumida");
            registro.setLocalEstoque(local);
            registro.setQuantidadeConsumida(quantidade);
            MovimentoEstoqueRequest movimento = new MovimentoEstoqueRequest();
            movimento.setItemId(item.getId()); movimento.setQuantidade(quantidade);
            movimento.setLocalOrigemId(local.getId()); movimento.setLoteCodigo(registro.getLoteEstoqueCodigo());
            movimento.setObservacao(registro.getObservacao());
            movimento.setDataMovimento(request.getDataProcedimento().atTime(12, 0));
            registro.setMovimentoEstoque(estoque.registrarConsumoSanidadeSuinos(
                    movimento, registro.getId(), alvoDescricao(registro)));
        }
        sincronizarTarefa(registro, ator);
        return resumo(registro);
    }

    @Transactional
    public RegistroSanitarioSuinosResumo concluirProximaAcao(Long id, UsuarioAtor ator) {
        RegistroSanitarioSuinos registro = registros.buscarParaAtualizacao(id)
                .orElseThrow(() -> naoEncontrado(id));
        if (registro.getProximaAcaoData() == null)
            throw erro("PROXIMA_ACAO_INEXISTENTE", "O registro não possui próxima ação.");
        if (!registro.isProximaAcaoConcluida()) {
            registro.setProximaAcaoConcluida(true);
            registro.setProximaAcaoConcluidaEm(LocalDateTime.now(clock));
            tarefas.concluirAutomatica(chaveTarefa(id), ator);
        }
        return resumo(registro);
    }

    private void sincronizarTarefa(RegistroSanitarioSuinos registro, UsuarioAtor ator) {
        if (registro.getProximaAcaoData() == null) return;
        tarefas.sincronizarAutomatica(new TarefaAutomaticaRequest(chaveTarefa(registro.getId()),
                registro.getProximaAcao(), "Acompanhamento sanitário de " + alvoDescricao(registro) + ".",
                PrioridadeTarefa.ALTA, registro.getProximaAcaoData().atTime(8, 0),
                ModuloOrigem.CRIACOES, referencia(registro.getId())), ator);
    }

    private void validar(RegistroSanitarioSuinosRequest request) {
        if ((request.getLoteId() == null) == (request.getAnimalReprodutivoId() == null))
            throw erro("ALVO_SANITARIO_INVALIDO", "Informe exatamente um lote ou animal reprodutivo.");
        if (request.getTipo() == null) throw erro("TIPO_OBRIGATORIO", "Informe o tipo de registro.");
        if (request.getDataProcedimento() == null || request.getDataProcedimento().isAfter(LocalDate.now(clock)))
            throw erro("DATA_PROCEDIMENTO_INVALIDA", "A data do procedimento é obrigatória e não pode estar no futuro.");
        boolean possuiAcao = StringUtils.hasText(request.getProximaAcao());
        if (possuiAcao != (request.getProximaAcaoData() != null))
            throw erro("PROXIMA_ACAO_INVALIDA", "Informe a próxima ação e sua data em conjunto.");
        if (request.getProximaAcaoData() != null && request.getProximaAcaoData().isBefore(request.getDataProcedimento()))
            throw erro("DATA_PROXIMA_ACAO_INVALIDA", "A próxima ação não pode preceder o procedimento.");
        if (request.getCusto() != null && request.getCusto().signum() < 0)
            throw erro("CUSTO_INVALIDO", "O custo não pode ser negativo.");
        if (request.getQuantidadeConsumida() != null
                && (request.getItemEstoqueId() == null || request.getLocalEstoqueId() == null))
            throw erro("CONSUMO_INCOMPLETO", "Para consumir estoque, informe item, local e quantidade.");
        if (request.getQuantidadeConsumida() == null && request.getLocalEstoqueId() != null)
            throw erro("CONSUMO_INCOMPLETO", "Informe a quantidade consumida para o local selecionado.");
    }

    private void validarFiltro(Long loteId, Long animalId) {
        if (loteId != null && animalId != null)
            throw erro("FILTRO_SANITARIO_INVALIDO", "Filtre por lote ou animal, não pelos dois.");
    }
    private void definirAlvo(RegistroSanitarioSuinos registro, Long loteId, Long animalId) {
        if (loteId != null) registro.setLote(lotes.findById(loteId).orElseThrow(() ->
                erro("LOTE_NAO_ENCONTRADO", "Lote de suínos não encontrado.")));
        else registro.setAnimalReprodutivo(animais.findById(animalId).orElseThrow(() ->
                erro("ANIMAL_NAO_ENCONTRADO", "Animal reprodutivo não encontrado.")));
    }
    private RegistroSanitarioSuinos buscar(Long id) { return registros.findById(id).orElseThrow(() -> naoEncontrado(id)); }
    private RegistroSanitarioSuinosResumo resumo(RegistroSanitarioSuinos r) {
        LoteSuinos lote = r.getLote(); AnimalReprodutivoSuinos animal = r.getAnimalReprodutivo();
        return new RegistroSanitarioSuinosResumo(r.getId(), r.getTipo(), r.getDataProcedimento(),
                r.getProcedimentoProduto(), r.getMotivo(), r.getResponsavel(), r.getObservacao(),
                lote == null ? null : lote.getId(), lote == null ? null : lote.getCodigo(),
                animal == null ? null : animal.getId(), animal == null ? null : animal.getCodigo(),
                animal == null ? null : animal.getIdentificacao(), r.getProximaAcao(), r.getProximaAcaoData(),
                r.isProximaAcaoConcluida(), r.getProximaAcaoConcluidaEm(), r.getCusto(),
                r.getItemEstoque() == null ? null : r.getItemEstoque().getId(),
                r.getItemEstoque() == null ? null : r.getItemEstoque().getNome(),
                r.getLocalEstoque() == null ? null : r.getLocalEstoque().getId(),
                r.getLocalEstoque() == null ? null : r.getLocalEstoque().getNome(),
                r.getQuantidadeConsumida(), r.getLoteEstoqueCodigo(),
                r.getMovimentoEstoque() == null ? null : r.getMovimentoEstoque().getId(),
                r.getVersao(), r.getCriadoEm(), r.getCriadoPor());
    }
    private String alvoDescricao(RegistroSanitarioSuinos r) {
        return r.getLote() != null ? "lote " + r.getLote().getCodigo()
                : "animal " + r.getAnimalReprodutivo().getCodigo();
    }
    public static String referencia(Long id) { return "SUINOS:SANIDADE:" + id; }
    public static String chaveTarefa(Long id) { return "CRIACAO:SUINOS:SANIDADE:" + id + ":PROXIMA_ACAO"; }
    private String obrigatorio(String v, String campo) { if (!StringUtils.hasText(v)) throw erro("CAMPO_OBRIGATORIO", campo + " é obrigatório."); return v.trim(); }
    private String texto(String v) { return StringUtils.hasText(v) ? v.trim() : null; }
    private BigDecimal positivo(BigDecimal v, String campo) { if (v == null || v.signum() <= 0) throw erro("VALOR_INVALIDO", campo + " deve ser maior que zero."); return v.setScale(4, RoundingMode.HALF_UP); }
    private BigDecimal dinheiro(BigDecimal v) { return v == null ? null : v.setScale(2, RoundingMode.HALF_UP); }
    private SuinosOperacaoException naoEncontrado(Long id) { return new SuinosOperacaoException("REGISTRO_SANITARIO_NAO_ENCONTRADO", "Registro sanitário não encontrado: " + id, HttpStatus.NOT_FOUND); }
    private SuinosOperacaoException erro(String c, String m) { return new SuinosOperacaoException(c, m); }
}
