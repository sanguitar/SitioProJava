package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.criacao.aves.dto.*;
import com.example.sitiopro.criacao.aves.entity.*;
import com.example.sitiopro.criacao.aves.repository.*;
import com.example.sitiopro.criacao.core.entity.InstalacaoCriacao;
import com.example.sitiopro.estoque.service.EstoqueMovimentoService;
import com.example.sitiopro.shared.observability.MdcScope;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.service.AlertaService;
import com.example.sitiopro.tarefas.service.TarefaService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LoteAvesService {
    private static final Logger log = LoggerFactory.getLogger(LoteAvesService.class);
    private static final int ESCALA = 4;

    private final LoteAvesRepository loteRepository;
    private final EventoLoteAvesRepository eventoRepository;
    private final AlimentacaoAvesRepository alimentacaoRepository;
    private final MortalidadeAvesRepository mortalidadeRepository;
    private final PesagemAvesRepository pesagemRepository;
    private final RegistroPosturaAvesRepository posturaRepository;
    private final TransferenciaLoteAvesRepository transferenciaRepository;
    private final InstalacaoCriacaoService instalacaoService;
    private final EstoqueMovimentoService estoqueService;
    private final AlertaService alertaService;
    private final TarefaService tarefaService;
    private final Clock clock;

    public LoteAvesService(LoteAvesRepository loteRepository, EventoLoteAvesRepository eventoRepository,
            AlimentacaoAvesRepository alimentacaoRepository, MortalidadeAvesRepository mortalidadeRepository,
            PesagemAvesRepository pesagemRepository, RegistroPosturaAvesRepository posturaRepository,
            TransferenciaLoteAvesRepository transferenciaRepository, InstalacaoCriacaoService instalacaoService,
            EstoqueMovimentoService estoqueService, AlertaService alertaService, TarefaService tarefaService,
            Clock clock) {
        this.loteRepository = loteRepository;
        this.eventoRepository = eventoRepository;
        this.alimentacaoRepository = alimentacaoRepository;
        this.mortalidadeRepository = mortalidadeRepository;
        this.pesagemRepository = pesagemRepository;
        this.posturaRepository = posturaRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.instalacaoService = instalacaoService;
        this.estoqueService = estoqueService;
        this.alertaService = alertaService;
        this.tarefaService = tarefaService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<LoteAvesResumo> listar(StatusLoteAves status, String termo, int pagina, int tamanho) {
        String busca = StringUtils.hasText(termo) ? termo.trim() : null;
        return PaginaResponse.de(loteRepository.buscar(status, busca,
                PageRequest.of(Math.max(0, pagina), Math.min(100, Math.max(1, tamanho)))).map(this::resumo));
    }

    @Transactional(readOnly = true)
    public List<LoteAvesResumo> listarAtivos() {
        return loteRepository.findByStatusOrderByCodigoAsc(StatusLoteAves.ATIVO).stream().map(this::resumo).toList();
    }

    @Transactional(readOnly = true)
    public LoteAvesDetalhe detalhar(Long id) {
        LoteAves lote = buscar(id);
        List<AlimentacaoAves> alimentacoes = alimentacaoRepository.findByLoteIdOrderByDataEventoDescIdDesc(id);
        List<PesagemAves> pesagens = pesagemRepository.findByLoteIdOrderByDataEventoDescIdDesc(id);
        LocalDate hoje = LocalDate.now(clock);
        String referencia = "LOTE:" + id;
        BigDecimal custoAlimentacao = alimentacoes.stream().map(this::custo).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal custoInicial = lote.getCustoInicial() == null ? BigDecimal.ZERO : lote.getCustoInicial();
        return new LoteAvesDetalhe(
                lote.getId(), lote.getCodigo(), lote.getNome(), lote.getEspecie(), lote.getFinalidade(),
                lote.getLinhagem(), lote.getOrigem(), lote.getDataEntrada(), lote.getDataNascimento(), idadeDias(lote, hoje),
                lote.getQuantidadeInicial(), lote.getQuantidadeAtual(), lote.getSexo(), lote.getInstalacaoAtual().getId(),
                lote.getInstalacaoAtual().getNome(), lote.getStatus(), lote.getObservacoes(), lote.getCustoInicial(),
                escala(custoAlimentacao), escala(custoInicial.add(custoAlimentacao)),
                indicadoresAlimentacao(lote, alimentacoes, hoje), indicadoresPostura(lote, hoje),
                pesagens.isEmpty() ? null : pesagem(pesagens.getFirst()),
                alimentacoes.stream().map(this::alimentacao).toList(),
                mortalidadeRepository.findByLoteIdOrderByDataEventoDescIdDesc(id).stream().map(this::mortalidade).toList(),
                pesagens.stream().map(this::pesagem).toList(),
                posturaRepository.findByLoteIdOrderByDataColetaDescIdDesc(id).stream().map(this::postura).toList(),
                transferenciaRepository.findByLoteIdOrderByDataEventoDescIdDesc(id).stream().map(this::transferencia).toList(),
                eventoRepository.findByLoteIdOrderByDataEventoDescIdDesc(id).stream().map(this::evento).toList(),
                alertaService.listarRelacionados(ModuloOrigem.CRIACOES, referencia),
                tarefaService.listarRelacionadas(ModuloOrigem.CRIACOES, referencia),
                lote.getVersao(), lote.getCriadoEm(), lote.getCriadoPor(), lote.getAlteradoEm(), lote.getAlteradoPor());
    }

    @Transactional(readOnly = true)
    public AtualizarLoteAvesRequest formularioEdicao(Long id) {
        LoteAves lote = buscar(id);
        AtualizarLoteAvesRequest r = new AtualizarLoteAvesRequest();
        r.setCodigo(lote.getCodigo()); r.setNome(lote.getNome()); r.setEspecie(lote.getEspecie());
        r.setFinalidade(lote.getFinalidade()); r.setLinhagem(lote.getLinhagem()); r.setOrigem(lote.getOrigem());
        r.setDataEntrada(lote.getDataEntrada()); r.setDataNascimento(lote.getDataNascimento());
        r.setSexo(lote.getSexo()); r.setObservacoes(lote.getObservacoes());
        return r;
    }

    @Transactional
    public LoteAvesDetalhe criar(CriarLoteAvesRequest request, UsuarioAtor ator) {
        exigirAdmin(ator);
        return criarInterno(request, ator.ator(), TipoEventoLoteAves.ENTRADA_INICIAL, null);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public LoteAves criarDeIncubacao(CriarLoteAvesRequest request, String ator, Long incubacaoId) {
        return criarEntidade(request, ator, TipoEventoLoteAves.ECLOSAO, "INCUBACAO:" + incubacaoId);
    }

    @Transactional
    public LoteAvesDetalhe atualizar(Long id, AtualizarLoteAvesRequest request, UsuarioAtor ator) {
        exigirAdmin(ator);
        LoteAves lote = buscarParaAtualizacao(id);
        String codigo = obrigatorio(request.getCodigo(), "Código");
        if (loteRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) throw conflito("LOTE_CODIGO_DUPLICADO", "Já existe lote com esse código.");
        validarDatas(request.getDataEntrada(), request.getDataNascimento());
        lote.setCodigo(codigo); lote.setNome(texto(request.getNome())); lote.setEspecie(obrigatorio(request.getEspecie(), "Espécie"));
        lote.setFinalidade(obrigatorio(request.getFinalidade(), "Finalidade")); lote.setLinhagem(texto(request.getLinhagem()));
        lote.setOrigem(obrigatorio(request.getOrigem(), "Origem")); lote.setDataEntrada(request.getDataEntrada());
        lote.setDataNascimento(request.getDataNascimento()); lote.setSexo(obrigatorio(request.getSexo(), "Sexo"));
        lote.setObservacoes(texto(request.getObservacoes()));
        log(lote, "criacao.aves.lote.updated", null);
        return detalhar(id);
    }

    @Transactional
    public LoteAvesDetalhe encerrar(Long id, EncerrarLoteAvesRequest request, UsuarioAtor ator) {
        exigirAdmin(ator);
        LoteAves lote = buscarParaAtualizacao(id);
        if (!lote.getStatus().ativo()) return detalhar(id);
        StatusLoteAves finalStatus = request.getStatusFinal();
        if (finalStatus == null || finalStatus == StatusLoteAves.ATIVO) throw new AvesOperacaoException("STATUS_FINAL_INVALIDO", "Informe um status final válido.");
        lote.setStatus(finalStatus);
        TipoEventoLoteAves tipo = switch (finalStatus) { case VENDIDO -> TipoEventoLoteAves.VENDA; case ABATIDO -> TipoEventoLoteAves.ABATE; default -> TipoEventoLoteAves.ENCERRAMENTO; };
        registrarEvento(lote, tipo, lote.getQuantidadeAtual(), LocalDateTime.now(clock), ator.ator(), texto(request.getObservacao()), null, lote.getInstalacaoAtual(), null);
        log(lote, "criacao.aves.lote.closed", lote.getQuantidadeAtual());
        return detalhar(id);
    }

    private LoteAvesDetalhe criarInterno(CriarLoteAvesRequest r, String ator, TipoEventoLoteAves tipo, String referencia) {
        LoteAves existente = loteRepository.findByChaveIdempotencia(chave(r.getChaveIdempotencia())).orElse(null);
        if (existente != null) return detalhar(existente.getId());
        return detalhar(criarEntidade(r, ator, tipo, referencia).getId());
    }

    private LoteAves criarEntidade(CriarLoteAvesRequest r, String ator, TipoEventoLoteAves tipo, String referencia) {
        validarCriacao(r);
        String codigo = obrigatorio(r.getCodigo(), "Código");
        if (loteRepository.existsByCodigoIgnoreCase(codigo)) throw conflito("LOTE_CODIGO_DUPLICADO", "Já existe lote com esse código.");
        InstalacaoCriacao instalacao = instalacaoService.buscarAtiva(r.getInstalacaoId());
        instalacaoService.validarCapacidade(instalacao, r.getQuantidadeInicial(), null);
        LoteAves lote = new LoteAves();
        lote.setCodigo(codigo); lote.setNome(texto(r.getNome())); lote.setEspecie(r.getEspecie()); lote.setFinalidade(r.getFinalidade());
        lote.setLinhagem(texto(r.getLinhagem())); lote.setOrigem(obrigatorio(r.getOrigem(), "Origem"));
        lote.setDataEntrada(r.getDataEntrada()); lote.setDataNascimento(r.getDataNascimento());
        lote.setQuantidadeInicial(r.getQuantidadeInicial()); lote.setQuantidadeAtual(r.getQuantidadeInicial());
        lote.setSexo(r.getSexo()); lote.setInstalacaoAtual(instalacao); lote.setStatus(StatusLoteAves.ATIVO);
        lote.setObservacoes(texto(r.getObservacoes())); lote.setCustoInicial(escala(r.getCustoInicial()));
        lote.setChaveIdempotencia(chave(r.getChaveIdempotencia()));
        lote = loteRepository.save(lote);
        registrarEvento(lote, tipo, lote.getQuantidadeInicial(), r.getDataEntrada().atStartOfDay(), ator,
                "Lote registrado com " + lote.getQuantidadeInicial() + " aves.", referencia, null, instalacao);
        log(lote, "criacao.aves.lote.created", lote.getQuantidadeInicial());
        return lote;
    }

    EventoLoteAves registrarEvento(LoteAves lote, TipoEventoLoteAves tipo, Integer quantidade,
            LocalDateTime data, String ator, String observacao, String referencia,
            InstalacaoCriacao origem, InstalacaoCriacao destino) {
        EventoLoteAves evento = new EventoLoteAves(); evento.setLote(lote); evento.setTipo(tipo); evento.setQuantidade(quantidade);
        evento.setDataEvento(data); evento.setOrigem("CRIACOES_AVES"); evento.setUsuario(ator);
        evento.setObservacao(observacao); evento.setReferenciaExterna(referencia);
        evento.setInstalacaoOrigem(origem); evento.setInstalacaoDestino(destino);
        return eventoRepository.save(evento);
    }

    LoteAves buscarParaAtualizacao(Long id) { return loteRepository.buscarParaAtualizacao(id).orElseThrow(() -> naoEncontrado(id)); }
    LoteAves buscar(Long id) { return loteRepository.findById(id).orElseThrow(() -> naoEncontrado(id)); }

    private void validarCriacao(CriarLoteAvesRequest r) {
        obrigatorio(r.getEspecie(), "Espécie"); obrigatorio(r.getFinalidade(), "Finalidade"); obrigatorio(r.getSexo(), "Sexo");
        if (r.getQuantidadeInicial() == null || r.getQuantidadeInicial() < 1) throw new AvesOperacaoException("QUANTIDADE_INVALIDA", "Quantidade inicial deve ser maior que zero.");
        if (r.getCustoInicial() != null && r.getCustoInicial().signum() < 0) throw new AvesOperacaoException("CUSTO_INVALIDO", "Custo inicial não pode ser negativo.");
        validarDatas(r.getDataEntrada(), r.getDataNascimento()); chave(r.getChaveIdempotencia());
    }

    private void validarDatas(LocalDate entrada, LocalDate nascimento) {
        if (entrada == null) throw new AvesOperacaoException("DATA_ENTRADA_OBRIGATORIA", "Informe a data de entrada.");
        if (nascimento != null && nascimento.isAfter(entrada)) throw new AvesOperacaoException("DATA_NASCIMENTO_INVALIDA", "Nascimento não pode ser posterior à entrada.");
    }

    private LoteAvesDetalhe.IndicadoresAlimentacao indicadoresAlimentacao(LoteAves lote, List<AlimentacaoAves> itens, LocalDate hoje) {
        Map<Long, List<AlimentacaoAves>> grupos = itens.stream().collect(Collectors.groupingBy(a -> a.getItemEstoque().getId(), LinkedHashMap::new, Collectors.toList()));
        List<LoteAvesDetalhe.ConsumoItemResumo> consumos = grupos.values().stream().map(grupo -> {
            AlimentacaoAves base = grupo.getFirst();
            BigDecimal total = grupo.stream().map(AlimentacaoAves::getQuantidade).reduce(BigDecimal.ZERO, BigDecimal::add);
            LocalDate primeira = grupo.stream().map(a -> a.getDataEvento().toLocalDate()).min(LocalDate::compareTo).orElse(hoje);
            long dias = Math.max(1, ChronoUnit.DAYS.between(primeira, hoje) + 1);
            BigDecimal media = total.divide(BigDecimal.valueOf(dias), ESCALA, RoundingMode.HALF_UP);
            BigDecimal porAve = lote.getQuantidadeAtual() > 0 ? total.divide(BigDecimal.valueOf(lote.getQuantidadeAtual()), ESCALA, RoundingMode.HALF_UP) : null;
            BigDecimal saldo = estoqueService.saldoItemTotal(base.getItemEstoque().getId());
            BigDecimal autonomia = media.signum() > 0 ? saldo.max(BigDecimal.ZERO).divide(media, 2, RoundingMode.HALF_UP) : null;
            return new LoteAvesDetalhe.ConsumoItemResumo(base.getItemEstoque().getId(), base.getItemEstoque().getNome(),
                    base.getItemEstoque().getUnidadeMedida().getSigla(), escala(total), media, porAve, escala(saldo), autonomia);
        }).toList();
        return new LoteAvesDetalhe.IndicadoresAlimentacao(consumos,
                escala(itens.stream().map(this::custo).reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    private LoteAvesDetalhe.IndicadoresPostura indicadoresPostura(LoteAves lote, LocalDate hoje) {
        long hojeTotal = posturaRepository.somarInteiros(lote.getId(), hoje, hoje);
        long sete = posturaRepository.somarInteiros(lote.getId(), hoje.minusDays(6), hoje);
        long trinta = posturaRepository.somarInteiros(lote.getId(), hoje.minusDays(29), hoje);
        BigDecimal media = BigDecimal.valueOf(trinta).divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
        BigDecimal porAve = lote.getQuantidadeAtual() > 0 ? BigDecimal.valueOf(hojeTotal).divide(BigDecimal.valueOf(lote.getQuantidadeAtual()), 4, RoundingMode.HALF_UP) : null;
        return new LoteAvesDetalhe.IndicadoresPostura(hojeTotal, sete, trinta, media, porAve);
    }

    private LoteAvesResumo resumo(LoteAves l) { return new LoteAvesResumo(l.getId(), l.getCodigo(), l.getNome(), l.getEspecie(), l.getFinalidade(), l.getQuantidadeAtual(), l.getInstalacaoAtual().getId(), l.getInstalacaoAtual().getNome(), l.getStatus(), l.getDataEntrada()); }
    private LoteAvesDetalhe.AlimentacaoResumo alimentacao(AlimentacaoAves a) { return new LoteAvesDetalhe.AlimentacaoResumo(a.getId(), a.getItemEstoque().getId(), a.getItemEstoque().getNome(), a.getQuantidade(), a.getItemEstoque().getUnidadeMedida().getSigla(), a.getLocalEstoque().getId(), a.getLocalEstoque().getNome(), a.getDataEvento(), a.getMovimentoEstoque() == null ? null : a.getMovimentoEstoque().getId(), custo(a), a.getObservacao()); }
    private LoteAvesDetalhe.MortalidadeResumo mortalidade(MortalidadeAves m) { return new LoteAvesDetalhe.MortalidadeResumo(m.getId(), m.getQuantidade(), m.getDataEvento(), m.getCausa(), m.getObservacao()); }
    private LoteAvesDetalhe.PesagemResumo pesagem(PesagemAves p) { return new LoteAvesDetalhe.PesagemResumo(p.getId(), p.getDataEvento(), p.getQuantidadeAmostrada(), p.getPesoMedio(), p.getPesoMinimo(), p.getPesoMaximo(), p.getObservacao()); }
    private LoteAvesDetalhe.PosturaResumo postura(RegistroPosturaAves p) { return new LoteAvesDetalhe.PosturaResumo(p.getId(), p.getDataColeta(), p.getOvosInteiros(), p.getOvosQuebrados(), p.getOvosDescartados(), p.getObservacao()); }
    private LoteAvesDetalhe.TransferenciaResumo transferencia(TransferenciaLoteAves t) { return new LoteAvesDetalhe.TransferenciaResumo(t.getId(), t.getInstalacaoOrigem().getId(), t.getInstalacaoOrigem().getNome(), t.getInstalacaoDestino().getId(), t.getInstalacaoDestino().getNome(), t.getDataEvento(), t.getUsuario(), t.getObservacao()); }
    private LoteAvesDetalhe.EventoResumo evento(EventoLoteAves e) { return new LoteAvesDetalhe.EventoResumo(e.getId(), e.getTipo(), e.getTipo().getRotulo(), e.getQuantidade(), e.getDataEvento(), e.getOrigem(), e.getUsuario(), e.getObservacao(), e.getReferenciaExterna(), e.getInstalacaoOrigem() == null ? null : e.getInstalacaoOrigem().getNome(), e.getInstalacaoDestino() == null ? null : e.getInstalacaoDestino().getNome()); }
    private BigDecimal custo(AlimentacaoAves a) { return a.getCustoUnitarioReferencia() == null ? BigDecimal.ZERO : a.getQuantidade().multiply(a.getCustoUnitarioReferencia()); }
    private Long idadeDias(LoteAves lote, LocalDate hoje) { LocalDate base = lote.getDataNascimento() != null ? lote.getDataNascimento() : lote.getDataEntrada(); return Math.max(0, ChronoUnit.DAYS.between(base, hoje)); }
    private BigDecimal escala(BigDecimal v) { return v == null ? null : v.setScale(ESCALA, RoundingMode.HALF_UP); }
    private String chave(String v) { return obrigatorio(v, "Chave de idempotência"); }
    private String texto(String v) { return StringUtils.hasText(v) ? v.trim() : null; }
    private String obrigatorio(String v, String campo) { if (!StringUtils.hasText(v)) throw new AvesOperacaoException("CAMPO_OBRIGATORIO", campo + " é obrigatório."); return v.trim(); }
    private <T> T obrigatorio(T v, String campo) { if (v == null) throw new AvesOperacaoException("CAMPO_OBRIGATORIO", campo + " é obrigatório."); return v; }
    private void exigirAdmin(UsuarioAtor ator) { if (!ator.admin()) throw new AvesOperacaoException("OPERACAO_ADMIN_OBRIGATORIA", "Operação restrita a administradores.", HttpStatus.FORBIDDEN); }
    private AvesOperacaoException naoEncontrado(Long id) { return new AvesOperacaoException("LOTE_NAO_ENCONTRADO", "Lote de aves não encontrado: " + id, HttpStatus.NOT_FOUND); }
    private AvesOperacaoException conflito(String c, String m) { return new AvesOperacaoException(c, m, HttpStatus.CONFLICT); }
    private void log(LoteAves lote, String evento, Integer qtd) { Map<String, Object> dados = new LinkedHashMap<>(); dados.put("event.action", evento); dados.put("module", "criacoes"); dados.put("criacao.aves.lote.id", String.valueOf(lote.getId())); if (qtd != null) dados.put("criacao.quantidade", qtd); try (MdcScope ignored = MdcScope.with(dados)) { log.info("Operação do lote de aves concluída."); } }
}
