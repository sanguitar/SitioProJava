package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.criacao.aves.dto.AcompanhamentoIncubacaoAvesResumo;
import com.example.sitiopro.criacao.aves.dto.RegistrarAcompanhamentoIncubacaoAvesRequest;
import com.example.sitiopro.criacao.aves.entity.AcompanhamentoIncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.IncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.MetodoIncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.StatusIncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.TipoAcompanhamentoIncubacaoAves;
import com.example.sitiopro.criacao.aves.repository.AcompanhamentoIncubacaoAvesRepository;
import com.example.sitiopro.criacao.aves.repository.IncubacaoAvesRepository;
import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.shared.observability.MdcScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class IncubacaoAcompanhamentoService {

    private static final Logger log = LoggerFactory.getLogger(IncubacaoAcompanhamentoService.class);

    private final IncubacaoAvesRepository incubacaoRepository;
    private final AcompanhamentoIncubacaoAvesRepository repository;
    private final CodigoCriacaoService codigoService;
    private final Clock clock;

    public IncubacaoAcompanhamentoService(IncubacaoAvesRepository incubacaoRepository,
            AcompanhamentoIncubacaoAvesRepository repository, CodigoCriacaoService codigoService, Clock clock) {
        this.incubacaoRepository = incubacaoRepository;
        this.repository = repository;
        this.codigoService = codigoService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<AcompanhamentoIncubacaoAvesResumo> listar(Long incubacaoId) {
        return repository.findByIncubacaoIdOrderByDataHoraDescIdDesc(incubacaoId).stream()
                .map(this::resumo)
                .toList();
    }

    @Transactional(readOnly = true)
    public AcompanhamentoIncubacaoAvesResumo detalhar(Long incubacaoId, Long acompanhamentoId) {
        AcompanhamentoIncubacaoAves acompanhamento = repository.findById(acompanhamentoId)
                .filter(item -> item.getIncubacao().getId().equals(incubacaoId))
                .orElseThrow(() -> new AvesOperacaoException("ACOMPANHAMENTO_NAO_ENCONTRADO",
                        "Acompanhamento não encontrado.", HttpStatus.NOT_FOUND));
        return resumo(acompanhamento);
    }

    @Transactional
    public AcompanhamentoIncubacaoAvesResumo registrar(Long incubacaoId,
            RegistrarAcompanhamentoIncubacaoAvesRequest request) {
        String chave = obrigatorio(request.getChaveIdempotencia(), "Chave de idempotência");
        codigoService.bloquearIdempotencia("ACOMPANHAMENTO_INCUBACAO_AVES", chave);
        AcompanhamentoIncubacaoAves existente = repository.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) {
            if (!existente.getIncubacao().getId().equals(incubacaoId)) {
                throw conflito("CHAVE_IDEMPOTENCIA_EM_USO", "A chave já foi usada em outra incubação.");
            }
            return resumo(existente);
        }

        IncubacaoAves incubacao = incubacaoRepository.buscarParaAtualizacao(incubacaoId)
                .orElseThrow(() -> new AvesOperacaoException("INCUBACAO_NAO_ENCONTRADA",
                        "Incubação não encontrada.", HttpStatus.NOT_FOUND));
        if (incubacao.getStatus() != StatusIncubacaoAves.EM_INCUBACAO) {
            throw conflito("INCUBACAO_ENCERRADA", "Acompanhamentos só podem ser registrados durante a incubação.");
        }
        validar(incubacao, request);

        AcompanhamentoIncubacaoAves acompanhamento = new AcompanhamentoIncubacaoAves();
        acompanhamento.setIncubacao(incubacao);
        acompanhamento.setDataHora(request.getDataHora());
        acompanhamento.setTipo(request.getTipo());
        acompanhamento.setQuantidadeAvaliada(request.getQuantidadeAvaliada());
        acompanhamento.setOvosFerteis(request.getOvosFerteis());
        acompanhamento.setOvosSemDesenvolvimento(request.getOvosSemDesenvolvimento());
        acompanhamento.setPerdas(request.getPerdas());
        acompanhamento.setTemperatura(request.getTemperatura());
        acompanhamento.setUmidade(request.getUmidade());
        acompanhamento.setObservacao(texto(request.getObservacao()));
        acompanhamento.setChaveIdempotencia(chave);
        acompanhamento = repository.save(acompanhamento);
        registrarLog(acompanhamento);
        return resumo(acompanhamento);
    }

    public RegistrarAcompanhamentoIncubacaoAvesRequest novo(TipoAcompanhamentoIncubacaoAves tipo) {
        RegistrarAcompanhamentoIncubacaoAvesRequest request = new RegistrarAcompanhamentoIncubacaoAvesRequest();
        request.setTipo(tipo);
        request.setDataHora(LocalDateTime.now(clock).withSecond(0).withNano(0));
        request.setChaveIdempotencia(java.util.UUID.randomUUID().toString());
        return request;
    }

    private void validar(IncubacaoAves incubacao, RegistrarAcompanhamentoIncubacaoAvesRequest request) {
        if (request.getTipo() == null || request.getDataHora() == null) {
            throw new AvesOperacaoException("ACOMPANHAMENTO_INCOMPLETO", "Informe tipo e data da verificação.");
        }
        if (request.getDataHora().toLocalDate().isBefore(incubacao.getDataInicio())) {
            throw new AvesOperacaoException("ACOMPANHAMENTO_ANTES_INICIO",
                    "A verificação não pode ser anterior ao início da incubação.");
        }
        validarNaoNegativo(request.getQuantidadeAvaliada(), "Quantidade avaliada");
        validarNaoNegativo(request.getOvosFerteis(), "Ovos férteis");
        validarNaoNegativo(request.getOvosSemDesenvolvimento(), "Ovos sem desenvolvimento");
        validarNaoNegativo(request.getPerdas(), "Perdas");

        int detalhados = valor(request.getOvosFerteis()) + valor(request.getOvosSemDesenvolvimento())
                + valor(request.getPerdas());
        if (request.getQuantidadeAvaliada() != null && detalhados > request.getQuantidadeAvaliada()) {
            throw new AvesOperacaoException("OVOSCOPIA_INCOERENTE",
                    "A soma dos resultados não pode superar a quantidade avaliada.");
        }
        if (request.getQuantidadeAvaliada() != null
                && request.getQuantidadeAvaliada() > incubacao.getQuantidadeOvos()) {
            throw new AvesOperacaoException("AVALIACAO_SUPERA_OVOS",
                    "A quantidade avaliada não pode superar os ovos inicialmente incubados.");
        }
        if (request.getTipo() == TipoAcompanhamentoIncubacaoAves.OVOSCOPIA
                && detalhados > 0 && request.getQuantidadeAvaliada() == null) {
            throw new AvesOperacaoException("QUANTIDADE_AVALIADA_OBRIGATORIA",
                    "Informe quantos ovos foram avaliados na ovoscopia.");
        }
        if (request.getTipo() == TipoAcompanhamentoIncubacaoAves.PERDA_RETIRADA
                && valor(request.getPerdas()) == 0) {
            throw new AvesOperacaoException("PERDA_OBRIGATORIA", "Informe a quantidade de perdas retiradas.");
        }

        boolean possuiMedicao = request.getTemperatura() != null || request.getUmidade() != null;
        if (request.getTipo() == TipoAcompanhamentoIncubacaoAves.TEMPERATURA_UMIDADE) {
            if (incubacao.getMetodo() != MetodoIncubacaoAves.CHOCADEIRA) {
                throw new AvesOperacaoException("MEDICAO_NAO_APLICAVEL",
                        "Temperatura e umidade manuais são registradas apenas para chocadeira.");
            }
            if (!possuiMedicao) {
                throw new AvesOperacaoException("MEDICAO_OBRIGATORIA", "Informe temperatura ou umidade.");
            }
            validarPercentual(request.getTemperatura(), "Temperatura");
            validarPercentual(request.getUmidade(), "Umidade");
        } else if (possuiMedicao) {
            throw new AvesOperacaoException("MEDICAO_EM_TIPO_INVALIDO",
                    "Use o registro de temperatura e umidade para informar medições.");
        }
    }

    private void validarNaoNegativo(Integer valor, String campo) {
        if (valor != null && valor < 0) {
            throw new AvesOperacaoException("QUANTIDADE_INVALIDA", campo + " não pode ser negativa.");
        }
    }

    private void validarPercentual(java.math.BigDecimal valor, String campo) {
        if (valor != null && (valor.signum() < 0 || valor.compareTo(java.math.BigDecimal.valueOf(100)) > 0)) {
            throw new AvesOperacaoException("MEDICAO_INVALIDA", campo + " deve estar entre 0 e 100.");
        }
    }

    private int valor(Integer valor) { return valor == null ? 0 : valor; }
    private String texto(String valor) { return StringUtils.hasText(valor) ? valor.trim() : null; }
    private String obrigatorio(String valor, String campo) {
        if (!StringUtils.hasText(valor)) throw new AvesOperacaoException("CAMPO_OBRIGATORIO", campo + " é obrigatória.");
        return valor.trim();
    }
    private AvesOperacaoException conflito(String codigo, String mensagem) {
        return new AvesOperacaoException(codigo, mensagem, HttpStatus.CONFLICT);
    }

    private AcompanhamentoIncubacaoAvesResumo resumo(AcompanhamentoIncubacaoAves item) {
        return new AcompanhamentoIncubacaoAvesResumo(item.getId(), item.getDataHora(), item.getTipo(),
                item.getQuantidadeAvaliada(), item.getOvosFerteis(), item.getOvosSemDesenvolvimento(),
                item.getPerdas(), item.getTemperatura(), item.getUmidade(), item.getObservacao(),
                item.getCriadoEm(), item.getCriadoPor());
    }

    private void registrarLog(AcompanhamentoIncubacaoAves item) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("event.action", "criacao.aves.incubation.check_recorded");
        dados.put("module", "criacoes");
        dados.put("criacao.aves.incubacao.id", item.getIncubacao().getId());
        dados.put("criacao.aves.acompanhamento.tipo", item.getTipo());
        try (MdcScope ignored = MdcScope.with(dados)) {
            log.info("Acompanhamento de incubação registrado.");
        }
    }
}
