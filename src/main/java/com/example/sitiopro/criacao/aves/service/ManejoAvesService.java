package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.criacao.aves.dto.*;
import com.example.sitiopro.criacao.aves.entity.*;
import com.example.sitiopro.criacao.aves.repository.*;
import com.example.sitiopro.criacao.core.entity.InstalacaoCriacao;
import com.example.sitiopro.estoque.dto.MovimentoEstoqueRequest;
import com.example.sitiopro.estoque.entity.ItemEstoque;
import com.example.sitiopro.estoque.entity.LocalEstoque;
import com.example.sitiopro.estoque.entity.MovimentoEstoque;
import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.estoque.service.EstoqueMovimentoService;
import com.example.sitiopro.shared.observability.MdcScope;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class ManejoAvesService {
    private static final Logger log = LoggerFactory.getLogger(ManejoAvesService.class);
    private final LoteAvesService loteService;
    private final MortalidadeAvesRepository mortalidadeRepository;
    private final AlimentacaoAvesRepository alimentacaoRepository;
    private final PesagemAvesRepository pesagemRepository;
    private final RegistroPosturaAvesRepository posturaRepository;
    private final TransferenciaLoteAvesRepository transferenciaRepository;
    private final InstalacaoCriacaoService instalacaoService;
    private final EstoqueCatalogoService estoqueCatalogoService;
    private final EstoqueMovimentoService estoqueMovimentoService;
    private final AvesAlertasService alertasService;
    private final Clock clock;

    public ManejoAvesService(LoteAvesService loteService, MortalidadeAvesRepository mortalidadeRepository,
            AlimentacaoAvesRepository alimentacaoRepository, PesagemAvesRepository pesagemRepository,
            RegistroPosturaAvesRepository posturaRepository, TransferenciaLoteAvesRepository transferenciaRepository,
            InstalacaoCriacaoService instalacaoService, EstoqueCatalogoService estoqueCatalogoService,
            EstoqueMovimentoService estoqueMovimentoService, AvesAlertasService alertasService, Clock clock) {
        this.loteService = loteService;
        this.mortalidadeRepository = mortalidadeRepository;
        this.alimentacaoRepository = alimentacaoRepository;
        this.pesagemRepository = pesagemRepository;
        this.posturaRepository = posturaRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.instalacaoService = instalacaoService;
        this.estoqueCatalogoService = estoqueCatalogoService;
        this.estoqueMovimentoService = estoqueMovimentoService;
        this.alertasService = alertasService;
        this.clock = clock;
    }

    @Transactional
    public LoteAvesDetalhe registrarMortalidade(Long loteId, RegistrarMortalidadeAvesRequest request, UsuarioAtor ator) {
        long inicio = System.nanoTime();
        String chave = chave(request.getChaveIdempotencia());
        MortalidadeAves existente = mortalidadeRepository.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) return idempotente(existente.getLote().getId(), loteId);
        LoteAves lote = loteAtivo(loteId);
        existente = mortalidadeRepository.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) return idempotente(existente.getLote().getId(), loteId);
        int quantidade = request.getQuantidade() == null ? 0 : request.getQuantidade();
        if (quantidade < 1) throw erro("MORTALIDADE_QUANTIDADE_INVALIDA", "Quantidade deve ser maior que zero.");
        if (quantidade > lote.getQuantidadeAtual()) throw conflito("MORTALIDADE_SUPERIOR_AO_LOTE", "Mortalidade não pode exceder a quantidade atual do lote.");
        LocalDateTime data = data(request.getDataEvento());
        lote.setQuantidadeAtual(lote.getQuantidadeAtual() - quantidade);
        EventoLoteAves evento = loteService.registrarEvento(lote, TipoEventoLoteAves.MORTALIDADE, quantidade, data,
                ator.ator(), texto(request.getObservacao()), null, lote.getInstalacaoAtual(), null);
        MortalidadeAves registro = new MortalidadeAves(); registro.setLote(lote); registro.setQuantidade(quantidade);
        registro.setDataEvento(data); registro.setCausa(texto(request.getCausa())); registro.setObservacao(texto(request.getObservacao()));
        registro.setChaveIdempotencia(chave); registro.setEvento(evento); mortalidadeRepository.save(registro);
        alertasService.avaliar();
        log("criacao.aves.mortality.registered", loteId, quantidade, inicio);
        return loteService.detalhar(loteId);
    }

    @Transactional
    public LoteAvesDetalhe registrarAlimentacao(Long loteId, RegistrarAlimentacaoAvesRequest request, UsuarioAtor ator) {
        long inicio = System.nanoTime();
        String chave = chave(request.getChaveIdempotencia());
        AlimentacaoAves existente = alimentacaoRepository.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) return idempotente(existente.getLote().getId(), loteId);
        LoteAves lote = loteAtivo(loteId);
        existente = alimentacaoRepository.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) return idempotente(existente.getLote().getId(), loteId);
        if (request.getQuantidade() == null || request.getQuantidade().signum() <= 0) throw erro("ALIMENTACAO_QUANTIDADE_INVALIDA", "Quantidade deve ser maior que zero.");
        ItemEstoque item = estoqueCatalogoService.buscarItem(request.getItemEstoqueId());
        LocalEstoque local = estoqueCatalogoService.buscarLocalAtivo(request.getLocalEstoqueId());
        LocalDateTime data = data(request.getDataEvento());
        AlimentacaoAves registro = new AlimentacaoAves(); registro.setLote(lote); registro.setItemEstoque(item);
        registro.setLocalEstoque(local); registro.setQuantidade(escala(request.getQuantidade())); registro.setDataEvento(data);
        registro.setObservacao(texto(request.getObservacao())); registro.setChaveIdempotencia(chave);
        registro.setCustoUnitarioReferencia(estoqueMovimentoService.custoMedio(item.getId()));
        registro = alimentacaoRepository.saveAndFlush(registro);

        MovimentoEstoqueRequest movimentoRequest = new MovimentoEstoqueRequest();
        movimentoRequest.setItemId(item.getId()); movimentoRequest.setQuantidade(registro.getQuantidade());
        movimentoRequest.setLocalOrigemId(local.getId()); movimentoRequest.setLoteCodigo(texto(request.getLoteEstoqueCodigo()));
        movimentoRequest.setDataMovimento(data); movimentoRequest.setObservacao("Consumo do lote de aves " + lote.getCodigo());
        MovimentoEstoque movimento = estoqueMovimentoService.registrarConsumoCriacao(movimentoRequest, registro.getId(), loteId);
        EventoLoteAves evento = loteService.registrarEvento(lote, TipoEventoLoteAves.ALIMENTACAO, null, data,
                ator.ator(), texto(request.getObservacao()), "ESTOQUE_MOVIMENTO:" + movimento.getId(), null, null);
        registro.setMovimentoEstoque(movimento); registro.setEvento(evento);
        log("criacao.aves.feed.registered", loteId, null, inicio);
        return loteService.detalhar(loteId);
    }

    @Transactional
    public LoteAvesDetalhe registrarPesagem(Long loteId, RegistrarPesagemAvesRequest request, UsuarioAtor ator) {
        long inicio = System.nanoTime(); String chave = chave(request.getChaveIdempotencia());
        PesagemAves existente = pesagemRepository.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) return idempotente(existente.getLote().getId(), loteId);
        LoteAves lote = loteAtivo(loteId);
        existente = pesagemRepository.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) return idempotente(existente.getLote().getId(), loteId);
        validarPesos(request);
        LocalDateTime data = data(request.getDataEvento());
        EventoLoteAves evento = loteService.registrarEvento(lote, TipoEventoLoteAves.PESAGEM, request.getQuantidadeAmostrada(), data, ator.ator(), texto(request.getObservacao()), null, null, null);
        PesagemAves registro = new PesagemAves(); registro.setLote(lote); registro.setDataEvento(data);
        registro.setQuantidadeAmostrada(request.getQuantidadeAmostrada()); registro.setPesoMedio(escala(request.getPesoMedio()));
        registro.setPesoMinimo(escala(request.getPesoMinimo())); registro.setPesoMaximo(escala(request.getPesoMaximo()));
        registro.setObservacao(texto(request.getObservacao())); registro.setChaveIdempotencia(chave); registro.setEvento(evento);
        pesagemRepository.save(registro); log("criacao.aves.weight.registered", loteId, request.getQuantidadeAmostrada(), inicio);
        return loteService.detalhar(loteId);
    }

    @Transactional
    public LoteAvesDetalhe registrarPostura(Long loteId, RegistrarPosturaAvesRequest request, UsuarioAtor ator) {
        long inicio = System.nanoTime(); String chave = chave(request.getChaveIdempotencia());
        RegistroPosturaAves existente = posturaRepository.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) return idempotente(existente.getLote().getId(), loteId);
        LoteAves lote = loteAtivo(loteId);
        existente = posturaRepository.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) return idempotente(existente.getLote().getId(), loteId);
        if (!lote.getFinalidade().permitePostura()) throw conflito("LOTE_NAO_PERMITE_POSTURA", "A finalidade do lote não permite registro de postura.");
        int inteiros = zero(request.getOvosInteiros()), quebrados = zero(request.getOvosQuebrados()), descartados = zero(request.getOvosDescartados());
        if (inteiros < 0 || quebrados < 0 || descartados < 0 || inteiros + quebrados + descartados == 0) throw erro("POSTURA_QUANTIDADE_INVALIDA", "Informe ao menos um ovo e não use valores negativos.");
        if (request.getDataColeta() == null) throw erro("POSTURA_DATA_OBRIGATORIA", "Informe a data da coleta.");
        LocalDateTime data = request.getDataEvento() == null ? request.getDataColeta().atStartOfDay() : request.getDataEvento();
        EventoLoteAves evento = loteService.registrarEvento(lote, TipoEventoLoteAves.POSTURA, inteiros + quebrados + descartados, data, ator.ator(), texto(request.getObservacao()), null, null, null);
        RegistroPosturaAves registro = new RegistroPosturaAves(); registro.setLote(lote); registro.setDataColeta(request.getDataColeta());
        registro.setOvosInteiros(inteiros); registro.setOvosQuebrados(quebrados); registro.setOvosDescartados(descartados);
        registro.setObservacao(texto(request.getObservacao())); registro.setChaveIdempotencia(chave); registro.setEvento(evento);
        posturaRepository.save(registro); log("criacao.aves.laying.registered", loteId, inteiros, inicio);
        return loteService.detalhar(loteId);
    }

    @Transactional
    public LoteAvesDetalhe transferir(Long loteId, TransferirLoteAvesRequest request, UsuarioAtor ator) {
        long inicio = System.nanoTime(); String chave = chave(request.getChaveIdempotencia());
        TransferenciaLoteAves existente = transferenciaRepository.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) return idempotente(existente.getLote().getId(), loteId);
        LoteAves lote = loteAtivo(loteId); InstalacaoCriacao origem = lote.getInstalacaoAtual();
        existente = transferenciaRepository.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) return idempotente(existente.getLote().getId(), loteId);
        InstalacaoCriacao destino = instalacaoService.reservarCapacidade(
                request.getInstalacaoDestinoId(), lote.getQuantidadeAtual(), lote.getId());
        if (Objects.equals(origem.getId(), destino.getId())) throw conflito("TRANSFERENCIA_MESMA_INSTALACAO", "Origem e destino devem ser diferentes.");
        LocalDateTime data = data(request.getDataEvento()); lote.setInstalacaoAtual(destino);
        EventoLoteAves evento = loteService.registrarEvento(lote, TipoEventoLoteAves.TRANSFERENCIA, lote.getQuantidadeAtual(), data, ator.ator(), texto(request.getObservacao()), null, origem, destino);
        TransferenciaLoteAves registro = new TransferenciaLoteAves(); registro.setLote(lote); registro.setInstalacaoOrigem(origem);
        registro.setInstalacaoDestino(destino); registro.setDataEvento(data); registro.setUsuario(ator.ator());
        registro.setObservacao(texto(request.getObservacao())); registro.setChaveIdempotencia(chave); registro.setEvento(evento);
        transferenciaRepository.save(registro); log("criacao.aves.transferred", loteId, lote.getQuantidadeAtual(), inicio);
        return loteService.detalhar(loteId);
    }

    private LoteAves loteAtivo(Long id) { LoteAves lote = loteService.buscarParaAtualizacao(id); if (!lote.getStatus().ativo()) throw conflito("LOTE_INATIVO", "O lote não aceita novas operações."); return lote; }
    private LoteAvesDetalhe idempotente(Long encontrado, Long esperado) { if (!Objects.equals(encontrado, esperado)) throw conflito("CHAVE_IDEMPOTENCIA_EM_USO", "A chave de idempotência já pertence a outro lote."); return loteService.detalhar(esperado); }
    private void validarPesos(RegistrarPesagemAvesRequest r) { if (r.getPesoMedio() == null || r.getPesoMedio().signum() <= 0) throw erro("PESO_MEDIO_INVALIDO", "Peso médio deve ser maior que zero."); if (r.getQuantidadeAmostrada() != null && r.getQuantidadeAmostrada() < 1) throw erro("AMOSTRA_INVALIDA", "Quantidade amostrada deve ser maior que zero."); if (r.getPesoMinimo() != null && r.getPesoMinimo().compareTo(r.getPesoMedio()) > 0) throw erro("PESO_MINIMO_INVALIDO", "Peso mínimo não pode superar o médio."); if (r.getPesoMaximo() != null && r.getPesoMaximo().compareTo(r.getPesoMedio()) < 0) throw erro("PESO_MAXIMO_INVALIDO", "Peso máximo não pode ser inferior ao médio."); }
    private int zero(Integer v) { return v == null ? 0 : v; }
    private LocalDateTime data(LocalDateTime v) { return v == null ? LocalDateTime.now(clock) : v; }
    private BigDecimal escala(BigDecimal v) { return v == null ? null : v.setScale(4, RoundingMode.HALF_UP); }
    private String texto(String v) { return StringUtils.hasText(v) ? v.trim() : null; }
    private String chave(String v) { if (!StringUtils.hasText(v)) throw erro("IDEMPOTENCIA_OBRIGATORIA", "Chave de idempotência é obrigatória."); return v.trim(); }
    private AvesOperacaoException erro(String c, String m) { return new AvesOperacaoException(c, m); }
    private AvesOperacaoException conflito(String c, String m) { return new AvesOperacaoException(c, m, HttpStatus.CONFLICT); }
    private void log(String evento, Long loteId, Integer quantidade, long inicio) { Map<String, Object> dados = new LinkedHashMap<>(); dados.put("event.action", evento); dados.put("module", "criacoes"); dados.put("criacao.aves.lote.id", String.valueOf(loteId)); dados.put("event.duration", System.nanoTime() - inicio); if (quantidade != null) dados.put("criacao.quantidade", quantidade); try (MdcScope ignored = MdcScope.with(dados)) { log.info("Operação de manejo de aves concluída."); } }
}
