package com.example.sitiopro.criacao.suinos.service;

import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.criacao.suinos.dto.*;
import com.example.sitiopro.criacao.suinos.entity.*;
import com.example.sitiopro.criacao.suinos.repository.*;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

@Service
public class SuinosReproducaoService {
    static final int DIAS_CHECAGEM = 28;
    static final int DIAS_GESTACAO = 114;
    static final int DIAS_DESMAME = 28;
    private static final Set<StatusCicloReprodutivoSuinos> CICLOS_ABERTOS = EnumSet.of(
            StatusCicloReprodutivoSuinos.AGUARDANDO_CHECAGEM,
            StatusCicloReprodutivoSuinos.GESTANTE,
            StatusCicloReprodutivoSuinos.PARTO_REALIZADO);

    private final AnimalReprodutivoSuinosRepository animais;
    private final CicloReprodutivoSuinosRepository ciclos;
    private final LoteSuinosRepository lotes;
    private final CodigoCriacaoService codigos;
    private final SuinosService suinos;
    private final SuinosReproducaoOperacionalService operacional;
    private final TarefaService tarefas;
    private final AlertaService alertas;
    private final Clock clock;

    public SuinosReproducaoService(AnimalReprodutivoSuinosRepository animais,
            CicloReprodutivoSuinosRepository ciclos, LoteSuinosRepository lotes,
            CodigoCriacaoService codigos, SuinosService suinos,
            SuinosReproducaoOperacionalService operacional, TarefaService tarefas,
            AlertaService alertas, Clock clock) {
        this.animais = animais; this.ciclos = ciclos; this.lotes = lotes; this.codigos = codigos;
        this.suinos = suinos; this.operacional = operacional; this.tarefas = tarefas;
        this.alertas = alertas; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<AnimalReprodutivoSuinosResumo> listarAnimais() {
        return animais.findByStatusOrderByTipoAscCodigoAsc(StatusAnimalReprodutivo.ATIVO).stream()
                .map(this::animal).toList();
    }

    @Transactional(readOnly = true)
    public List<AnimalReprodutivoSuinosResumo> listarMatrizes() {
        return animais.findByTipoAndStatusOrderByCodigoAsc(TipoAnimalReprodutivo.MATRIZ,
                StatusAnimalReprodutivo.ATIVO).stream().map(this::animal).toList();
    }

    @Transactional(readOnly = true)
    public List<AnimalReprodutivoSuinosResumo> listarReprodutores() {
        return animais.findByTipoAndStatusOrderByCodigoAsc(TipoAnimalReprodutivo.REPRODUTOR,
                StatusAnimalReprodutivo.ATIVO).stream().map(this::animal).toList();
    }

    @Transactional(readOnly = true)
    public AnimalReprodutivoSuinosResumo detalharAnimal(Long id) { return animal(buscarAnimal(id)); }

    @Transactional(readOnly = true)
    public List<CicloReprodutivoSuinosResumo> listarCiclos() {
        return ciclos.findAllByOrderByDataCoberturaDescIdDesc().stream().map(this::resumo).toList();
    }

    @Transactional(readOnly = true)
    public List<CicloReprodutivoSuinosResumo> historicoMatriz(Long matrizId) {
        buscarAnimal(matrizId);
        return ciclos.findByMatrizIdOrderByDataCoberturaDescIdDesc(matrizId).stream().map(this::resumo).toList();
    }

    @Transactional(readOnly = true)
    public CicloReprodutivoSuinosDetalhe detalhar(Long id) { return detalhe(buscarCiclo(id)); }

    @Transactional(readOnly = true)
    public ReproducaoSuinosResumo resumoOperacional() {
        LocalDate hoje = LocalDate.now(clock);
        long atrasos = ciclos.findByStatusAndDataPrevistaChecagemBefore(
                StatusCicloReprodutivoSuinos.AGUARDANDO_CHECAGEM, hoje).size()
                + ciclos.findByStatusAndDataPrevistaPartoBefore(StatusCicloReprodutivoSuinos.GESTANTE, hoje).size();
        return new ReproducaoSuinosResumo(
                animais.countByTipoAndStatus(TipoAnimalReprodutivo.MATRIZ, StatusAnimalReprodutivo.ATIVO),
                animais.countByTipoAndStatus(TipoAnimalReprodutivo.REPRODUTOR, StatusAnimalReprodutivo.ATIVO),
                ciclos.countByStatus(StatusCicloReprodutivoSuinos.AGUARDANDO_CHECAGEM),
                ciclos.countByStatus(StatusCicloReprodutivoSuinos.GESTANTE),
                ciclos.countByStatusAndDataPrevistaPartoBetween(StatusCicloReprodutivoSuinos.GESTANTE,
                        hoje, hoje.plusDays(7)), atrasos);
    }

    @Transactional
    public AnimalReprodutivoSuinosResumo cadastrarAnimal(CriarAnimalReprodutivoRequest request, UsuarioAtor ator) {
        exigirAdmin(ator);
        String chave = chave(request.getChaveIdempotencia());
        codigos.bloquearIdempotencia("SUINOS_ANIMAL", chave);
        AnimalReprodutivoSuinos existente = animais.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) return animal(existente);
        LoteSuinos lote = lotes.findById(request.getLoteId()).orElseThrow(() -> erro("LOTE_NAO_ENCONTRADO", "Lote não encontrado."));
        CategoriaSuino categoriaEsperada = request.getTipo() == TipoAnimalReprodutivo.MATRIZ
                ? CategoriaSuino.MATRIZ : CategoriaSuino.REPRODUTOR;
        if (lote.getStatus() != StatusLoteSuinos.ATIVO || lote.getCategoria() != categoriaEsperada)
            throw erro("LOTE_REPRODUTIVO_INVALIDO", "O animal deve pertencer a um lote ativo da categoria correspondente.");
        if (animais.countByLoteIdAndStatus(lote.getId(), StatusAnimalReprodutivo.ATIVO) >= lote.getQuantidadeAtual())
            throw erro("LOTE_SEM_ANIMAIS_DISPONIVEIS", "Todos os animais atuais do lote já estão identificados.");
        validarData(request.getDataNascimento(), "nascimento");
        AnimalReprodutivoSuinos animal = new AnimalReprodutivoSuinos();
        animal.setCodigo(codigos.proximoAnimalSuinos()); animal.setLote(lote); animal.setTipo(request.getTipo());
        animal.setIdentificacao(texto(request.getIdentificacao())); animal.setDataNascimento(request.getDataNascimento());
        animal.setPesoAtual(escalaPositivaOpcional(request.getPesoAtual(), "Peso"));
        animal.setStatus(StatusAnimalReprodutivo.ATIVO); animal.setObservacao(texto(request.getObservacao()));
        animal.setChaveIdempotencia(chave);
        return animal(animais.save(animal));
    }

    @Transactional
    public CicloReprodutivoSuinosDetalhe registrarCobertura(RegistrarCoberturaSuinosRequest request, UsuarioAtor ator) {
        String chave = chave(request.getChaveIdempotencia());
        codigos.bloquearIdempotencia("SUINOS_REPRODUCAO", chave);
        CicloReprodutivoSuinos existente = ciclos.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) return detalhe(existente);
        AnimalReprodutivoSuinos matriz = animalAtivo(request.getMatrizId(), TipoAnimalReprodutivo.MATRIZ);
        if (ciclos.existsByMatrizIdAndStatusIn(matriz.getId(), CICLOS_ABERTOS))
            throw erro("MATRIZ_COM_CICLO_ABERTO", "A matriz já possui um ciclo reprodutivo aberto.");
        AnimalReprodutivoSuinos reprodutor = request.getReprodutorId() == null ? null
                : animalAtivo(request.getReprodutorId(), TipoAnimalReprodutivo.REPRODUTOR);
        if (request.getMetodo() == MetodoReproducaoSuinos.COBERTURA && reprodutor == null)
            throw erro("REPRODUTOR_OBRIGATORIO", "Informe o reprodutor usado na cobertura.");
        validarDataObrigatoria(request.getDataCobertura(), "cobertura");
        CicloReprodutivoSuinos ciclo = new CicloReprodutivoSuinos();
        ciclo.setCodigo(codigos.proximaReproducaoSuinos()); ciclo.setMatriz(matriz); ciclo.setReprodutor(reprodutor);
        ciclo.setMetodo(request.getMetodo()); ciclo.setDataCobertura(request.getDataCobertura());
        ciclo.setDataPrevistaChecagem(request.getDataCobertura().plusDays(DIAS_CHECAGEM));
        ciclo.setDataPrevistaParto(request.getDataCobertura().plusDays(DIAS_GESTACAO));
        ciclo.setStatus(StatusCicloReprodutivoSuinos.AGUARDANDO_CHECAGEM);
        ciclo.setObservacao(texto(request.getObservacao())); ciclo.setChaveIdempotencia(chave);
        ciclo = ciclos.save(ciclo); operacional.garantirTarefas(ciclo, ator);
        return detalhe(ciclo);
    }

    @Transactional
    public CicloReprodutivoSuinosDetalhe registrarChecagem(Long id, ChecagemGestacaoSuinosRequest request,
            UsuarioAtor ator) {
        CicloReprodutivoSuinos ciclo = cicloParaAtualizacao(id);
        if (ciclo.getStatus() != StatusCicloReprodutivoSuinos.AGUARDANDO_CHECAGEM)
            throw conflito("CHECAGEM_JA_REGISTRADA", "A checagem deste ciclo já foi registrada.");
        validarDataObrigatoria(request.getDataChecagem(), "checagem");
        if (request.getDataChecagem().isBefore(ciclo.getDataCobertura()))
            throw erro("DATA_CHECAGEM_INVALIDA", "A checagem não pode preceder a cobertura.");
        ciclo.setDataChecagem(request.getDataChecagem());
        ciclo.setStatus(Boolean.TRUE.equals(request.getGestacaoConfirmada())
                ? StatusCicloReprodutivoSuinos.GESTANTE : StatusCicloReprodutivoSuinos.NAO_CONFIRMADA);
        acrescentarObservacao(ciclo, request.getObservacao());
        operacional.garantirTarefas(ciclo, ator);
        return detalhe(ciclo);
    }

    @Transactional
    public CicloReprodutivoSuinosDetalhe registrarParto(Long id, RegistrarPartoSuinosRequest request,
            UsuarioAtor ator) {
        String chave = chave(request.getChaveIdempotencia());
        codigos.bloquearIdempotencia("SUINOS_PARTO", chave);
        CicloReprodutivoSuinos repetido = ciclos.findByChaveParto(chave).orElse(null);
        if (repetido != null) return detalhe(repetido);
        CicloReprodutivoSuinos ciclo = cicloParaAtualizacao(id);
        if (ciclo.getStatus() != StatusCicloReprodutivoSuinos.GESTANTE)
            throw conflito("CICLO_NAO_GESTANTE", "O parto exige uma gestação confirmada.");
        validarDataObrigatoria(request.getDataParto(), "parto");
        if (request.getDataParto().isBefore(ciclo.getDataCobertura()))
            throw erro("DATA_PARTO_INVALIDA", "O parto não pode preceder a cobertura.");
        int vivos = naoNegativo(request.getNascidosVivos(), "Nascidos vivos");
        int natimortos = naoNegativo(request.getNatimortos(), "Natimortos");
        int perdas = naoNegativo(request.getPerdas(), "Perdas");
        if (vivos + natimortos + perdas == 0)
            throw erro("PARTO_SEM_RESULTADO", "Informe ao menos um nascimento ou perda.");
        ciclo.setDataParto(request.getDataParto()); ciclo.setNascidosVivos(vivos);
        ciclo.setNatimortos(natimortos); ciclo.setPerdasParto(perdas); ciclo.setChaveParto(chave);
        ciclo.setObservacao(texto(request.getObservacao()));
        if (vivos > 0) {
            if (request.getInstalacaoLeitoesId() == null)
                throw erro("INSTALACAO_LEITOES_OBRIGATORIA", "Informe a instalação dos leitões.");
            LoteSuinosDetalhe lote = suinos.criarLoteDoParto(request.getInstalacaoLeitoesId(), vivos,
                    request.getDataParto(), request.getPesoMedioLeitoes(), "Parto " + ciclo.getCodigo(),
                    request.getObservacao(), chaveLote(ciclo.getId(), chave), ator);
            ciclo.setLoteLeitoes(lotes.getReferenceById(lote.id()));
            ciclo.setDataPrevistaDesmame(request.getDataParto().plusDays(DIAS_DESMAME));
            ciclo.setStatus(StatusCicloReprodutivoSuinos.PARTO_REALIZADO);
        } else {
            ciclo.setStatus(StatusCicloReprodutivoSuinos.ENCERRADO);
        }
        ciclos.flush(); operacional.garantirTarefas(ciclo, ator);
        return detalhe(ciclo);
    }

    @Transactional
    public CicloReprodutivoSuinosDetalhe registrarDesmame(Long id, RegistrarDesmameSuinosRequest request,
            UsuarioAtor ator) {
        String chave = chave(request.getChaveIdempotencia());
        codigos.bloquearIdempotencia("SUINOS_DESMAME", chave);
        CicloReprodutivoSuinos repetido = ciclos.findByChaveDesmame(chave).orElse(null);
        if (repetido != null) return detalhe(repetido);
        CicloReprodutivoSuinos ciclo = cicloParaAtualizacao(id);
        if (ciclo.getStatus() != StatusCicloReprodutivoSuinos.PARTO_REALIZADO || ciclo.getLoteLeitoes() == null)
            throw conflito("PARTO_NAO_REGISTRADO", "O desmame exige um parto com lote de leitões.");
        validarDataObrigatoria(request.getDataDesmame(), "desmame");
        if (request.getDataDesmame().isBefore(ciclo.getDataParto()))
            throw erro("DATA_DESMAME_INVALIDA", "O desmame não pode preceder o parto.");
        LocalDateTime momento = request.getDataDesmame().atTime(12, 0);
        String observacao = texto(request.getObservacao());
        if (request.getPesoMedio() != null) {
            PesagemSuinosRequest pesagem = new PesagemSuinosRequest(); pesagem.setDataEvento(momento);
            pesagem.setPesoMedio(request.getPesoMedio()); pesagem.setObservacao(observacao);
            pesagem.setChaveIdempotencia(chaveEvento(chave, "PESO"));
            suinos.registrarPesagem(ciclo.getLoteLeitoes().getId(), pesagem, ator);
        }
        if (request.getInstalacaoDestinoId() != null
                && !request.getInstalacaoDestinoId().equals(ciclo.getLoteLeitoes().getInstalacaoAtual().getId())) {
            TransferenciaSuinosRequest transferencia = new TransferenciaSuinosRequest(); transferencia.setDataEvento(momento);
            transferencia.setInstalacaoDestinoId(request.getInstalacaoDestinoId()); transferencia.setObservacao(observacao);
            transferencia.setChaveIdempotencia(chaveEvento(chave, "TRANSF"));
            suinos.transferir(ciclo.getLoteLeitoes().getId(), transferencia, ator);
        }
        ciclo.setDataDesmame(request.getDataDesmame());
        ciclo.setPesoMedioDesmame(escalaPositivaOpcional(request.getPesoMedio(), "Peso médio"));
        ciclo.setChaveDesmame(chave); ciclo.setStatus(StatusCicloReprodutivoSuinos.DESMAMADO);
        acrescentarObservacao(ciclo, observacao);
        return detalhe(ciclo);
    }

    private CicloReprodutivoSuinosDetalhe detalhe(CicloReprodutivoSuinos c) {
        String ref = SuinosReproducaoOperacionalService.referencia(c.getId());
        return new CicloReprodutivoSuinosDetalhe(c.getId(), c.getCodigo(), animal(c.getMatriz()),
                c.getReprodutor() == null ? null : animal(c.getReprodutor()), c.getMetodo(), c.getDataCobertura(),
                c.getDataPrevistaChecagem(), c.getDataChecagem(), c.getDataPrevistaParto(), c.getDataParto(),
                c.getNascidosVivos(), c.getNatimortos(), c.getPerdasParto(),
                c.getLoteLeitoes() == null ? null : c.getLoteLeitoes().getId(),
                c.getLoteLeitoes() == null ? null : c.getLoteLeitoes().getCodigo(), c.getDataPrevistaDesmame(),
                c.getDataDesmame(), c.getPesoMedioDesmame(), c.getStatus(), c.getObservacao(),
                tarefas.listarRelacionadas(ModuloOrigem.CRIACOES, ref),
                alertas.listarRelacionados(ModuloOrigem.CRIACOES, ref), c.getVersao(),
                c.getCriadoEm(), c.getCriadoPor(), c.getAlteradoEm(), c.getAlteradoPor());
    }

    private CicloReprodutivoSuinosResumo resumo(CicloReprodutivoSuinos c) {
        return new CicloReprodutivoSuinosResumo(c.getId(), c.getCodigo(), c.getMatriz().getId(),
                c.getMatriz().getCodigo(), c.getMatriz().getIdentificacao(), c.getMetodo(), c.getDataCobertura(),
                c.getDataPrevistaChecagem(), c.getDataPrevistaParto(), c.getDataParto(), c.getStatus(),
                c.getNascidosVivos(), c.getLoteLeitoes() == null ? null : c.getLoteLeitoes().getId(),
                c.getLoteLeitoes() == null ? null : c.getLoteLeitoes().getCodigo(), c.getDataPrevistaDesmame());
    }

    private AnimalReprodutivoSuinosResumo animal(AnimalReprodutivoSuinos a) {
        return new AnimalReprodutivoSuinosResumo(a.getId(), a.getCodigo(), a.getIdentificacao(), a.getTipo(),
                a.getStatus(), a.getLote().getId(), a.getLote().getCodigo(), a.getDataNascimento(),
                a.getPesoAtual(), a.getVersao());
    }

    private AnimalReprodutivoSuinos animalAtivo(Long id, TipoAnimalReprodutivo tipo) {
        AnimalReprodutivoSuinos animal = buscarAnimal(id);
        if (animal.getStatus() != StatusAnimalReprodutivo.ATIVO || animal.getTipo() != tipo)
            throw erro("ANIMAL_REPRODUTIVO_INVALIDO", "Animal reprodutivo inválido ou inativo.");
        return animal;
    }
    private AnimalReprodutivoSuinos buscarAnimal(Long id) { return animais.findById(id).orElseThrow(() -> erro("ANIMAL_NAO_ENCONTRADO", "Animal reprodutivo não encontrado.")); }
    private CicloReprodutivoSuinos buscarCiclo(Long id) { return ciclos.findById(id).orElseThrow(() -> erro("CICLO_NAO_ENCONTRADO", "Ciclo reprodutivo não encontrado.")); }
    private CicloReprodutivoSuinos cicloParaAtualizacao(Long id) { return ciclos.buscarParaAtualizacao(id).orElseThrow(() -> erro("CICLO_NAO_ENCONTRADO", "Ciclo reprodutivo não encontrado.")); }
    private void validarDataObrigatoria(LocalDate data, String nome) { if (data == null) throw erro("DATA_OBRIGATORIA", "Informe a data de " + nome + "."); validarData(data, nome); }
    private void validarData(LocalDate data, String nome) { if (data != null && data.isAfter(LocalDate.now(clock))) throw erro("DATA_FUTURA", "A data de " + nome + " não pode estar no futuro."); }
    private int naoNegativo(Integer valor, String campo) { if (valor == null || valor < 0) throw erro("QUANTIDADE_INVALIDA", campo + " não pode ser negativo."); return valor; }
    private BigDecimal escalaPositivaOpcional(BigDecimal valor, String campo) { if (valor == null) return null; if (valor.signum() <= 0) throw erro("VALOR_INVALIDO", campo + " deve ser maior que zero."); return valor.setScale(4, RoundingMode.HALF_UP); }
    private String texto(String valor) { return StringUtils.hasText(valor) ? valor.trim() : null; }
    private String chave(String valor) { if (!StringUtils.hasText(valor)) throw erro("IDEMPOTENCIA_OBRIGATORIA", "Chave de idempotência é obrigatória."); return valor.trim(); }
    private String chaveLote(Long id, String chave) { return chaveDerivada("PARTO-" + id, chave); }
    private String chaveEvento(String chave, String sufixo) { return chaveDerivada("DESMAME-" + sufixo, chave); }
    private String chaveDerivada(String prefixo, String chave) {
        return prefixo + "-" + UUID.nameUUIDFromBytes(chave.getBytes(StandardCharsets.UTF_8));
    }
    private void acrescentarObservacao(CicloReprodutivoSuinos ciclo, String observacao) { if (StringUtils.hasText(observacao)) ciclo.setObservacao(texto(observacao)); }
    private void exigirAdmin(UsuarioAtor ator) { if (!ator.admin()) throw new SuinosOperacaoException("ADMIN_OBRIGATORIO", "Operação restrita a administradores.", HttpStatus.FORBIDDEN); }
    private SuinosOperacaoException erro(String codigo, String mensagem) { return new SuinosOperacaoException(codigo, mensagem); }
    private SuinosOperacaoException conflito(String codigo, String mensagem) { return new SuinosOperacaoException(codigo, mensagem, HttpStatus.CONFLICT); }
}
