package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.criacao.aves.config.AvesProperties;
import com.example.sitiopro.criacao.aves.dto.FichaOvoscopiaIncubacaoAves;
import com.example.sitiopro.criacao.aves.dto.ItemOvoscopiaIncubacaoAvesRequest;
import com.example.sitiopro.criacao.aves.dto.OvoIncubacaoAvesResumo;
import com.example.sitiopro.criacao.aves.dto.OvoscopiaIncubacaoAvesResumo;
import com.example.sitiopro.criacao.aves.dto.RegistrarOvoscopiaIncubacaoAvesRequest;
import com.example.sitiopro.criacao.aves.entity.AchadoOvoscopiaAves;
import com.example.sitiopro.criacao.aves.entity.IncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.ItemOvoscopiaIncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.OvoIncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.OvoscopiaIncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.StatusIncubacaoAves;
import com.example.sitiopro.criacao.aves.repository.IncubacaoAvesRepository;
import com.example.sitiopro.criacao.aves.repository.OvoIncubacaoAvesRepository;
import com.example.sitiopro.criacao.aves.repository.OvoscopiaIncubacaoAvesRepository;
import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.tarefas.dto.TarefaAutomaticaRequest;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;
import com.example.sitiopro.tarefas.service.TarefaService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OvoscopiaIncubacaoAvesService {
    private final IncubacaoAvesRepository incubacaoRepository;
    private final OvoIncubacaoAvesRepository ovoRepository;
    private final OvoscopiaIncubacaoAvesRepository ovoscopiaRepository;
    private final CodigoCriacaoService codigoService;
    private final TarefaService tarefaService;
    private final AvesProperties properties;
    private final Clock clock;

    public OvoscopiaIncubacaoAvesService(IncubacaoAvesRepository incubacaoRepository,
            OvoIncubacaoAvesRepository ovoRepository,
            OvoscopiaIncubacaoAvesRepository ovoscopiaRepository,
            CodigoCriacaoService codigoService,
            TarefaService tarefaService,
            AvesProperties properties,
            Clock clock) {
        this.incubacaoRepository = incubacaoRepository;
        this.ovoRepository = ovoRepository;
        this.ovoscopiaRepository = ovoscopiaRepository;
        this.codigoService = codigoService;
        this.tarefaService = tarefaService;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public void garantirOvos(IncubacaoAves incubacao) {
        long existentes = ovoRepository.countByIncubacaoId(incubacao.getId());
        if (existentes == incubacao.getQuantidadeOvos()) {
            return;
        }
        List<OvoIncubacaoAves> ovos = ovoRepository.findByIncubacaoIdOrderByNumeroAsc(incubacao.getId());
        LinkedHashSet<Integer> cadastrados = ovos.stream()
                .map(OvoIncubacaoAves::getNumero)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<OvoIncubacaoAves> novos = new ArrayList<>();
        for (int numero = 1; numero <= incubacao.getQuantidadeOvos(); numero++) {
            if (!cadastrados.contains(numero)) {
                OvoIncubacaoAves ovo = new OvoIncubacaoAves();
                ovo.setIncubacao(incubacao);
                ovo.setNumero(numero);
                novos.add(ovo);
            }
        }
        ovoRepository.saveAll(novos);
    }

    @Transactional(readOnly = true)
    public List<OvoIncubacaoAvesResumo> ovos(Long incubacaoId) {
        List<OvoIncubacaoAves> ovos = ovoRepository.findByIncubacaoIdOrderByNumeroAsc(incubacaoId);
        Map<Long, ItemOvoscopiaIncubacaoAves> ultimos = ultimosPorOvo(incubacaoId);
        return ovos.stream()
                .map(ovo -> resumo(ovo, ultimos.get(ovo.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OvoscopiaIncubacaoAvesResumo> ovoscopias(Long incubacaoId) {
        return ovoscopiaRepository.findByIncubacaoIdOrderByDataOvoscopiaDescIdDesc(incubacaoId)
                .stream().map(this::resumo).toList();
    }

    @Transactional
    public OvoscopiaIncubacaoAvesResumo registrar(Long incubacaoId,
            RegistrarOvoscopiaIncubacaoAvesRequest request, UsuarioAtor ator) {
        String chave = obrigatorio(request.getChaveIdempotencia(), "Chave de idempotência");
        codigoService.bloquearIdempotencia("OVOSCOPIA_INCUBACAO_AVES", chave);
        OvoscopiaIncubacaoAves existente = ovoscopiaRepository.findByChaveIdempotencia(chave).orElse(null);
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
            throw conflito("INCUBACAO_ENCERRADA", "Ovoscopia só pode ser registrada durante a incubação.");
        }
        garantirOvos(incubacao);
        validar(incubacao, request);

        Map<Integer, OvoIncubacaoAves> ovos = ovoRepository.findByIncubacaoIdOrderByNumeroAsc(incubacaoId)
                .stream().collect(Collectors.toMap(OvoIncubacaoAves::getNumero, Function.identity()));
        OvoscopiaIncubacaoAves ovoscopia = new OvoscopiaIncubacaoAves();
        ovoscopia.setIncubacao(incubacao);
        ovoscopia.setDataOvoscopia(request.getDataOvoscopia());
        ovoscopia.setDiaIncubacao(diaIncubacao(incubacao, request.getDataOvoscopia()));
        ovoscopia.setProximaVerificacao(proximaVerificacao(incubacao, request));
        ovoscopia.setResponsavel(texto(request.getResponsavel()));
        ovoscopia.setObservacaoGeral(texto(request.getObservacaoGeral()));
        ovoscopia.setChaveIdempotencia(chave);
        for (ItemOvoscopiaIncubacaoAvesRequest itemRequest : request.getItens()) {
            ItemOvoscopiaIncubacaoAves item = new ItemOvoscopiaIncubacaoAves();
            item.setOvoscopia(ovoscopia);
            item.setOvo(ovos.get(itemRequest.getNumero()));
            item.setAchado(itemRequest.getAchado());
            item.setObservacao(texto(itemRequest.getObservacao()));
            ovoscopia.getItens().add(item);
        }
        ovoscopia = ovoscopiaRepository.save(ovoscopia);
        if (ovoscopia.getItens().stream().anyMatch(item -> item.getAchado() == AchadoOvoscopiaAves.REAVALIAR)) {
            sincronizarTarefaReavaliacao(incubacao, ovoscopia, ator);
        }
        return resumo(ovoscopia);
    }

    public RegistrarOvoscopiaIncubacaoAvesRequest novoFormulario(Long incubacaoId) {
        RegistrarOvoscopiaIncubacaoAvesRequest request = new RegistrarOvoscopiaIncubacaoAvesRequest();
        request.setDataOvoscopia(LocalDate.now(clock));
        request.setProximaVerificacao(LocalDate.now(clock).plusDays(properties.getTarefaOvoscopiaDias()));
        request.setChaveIdempotencia(UUID.randomUUID().toString());
        ovos(incubacaoId).forEach(ovo -> {
            ItemOvoscopiaIncubacaoAvesRequest item = new ItemOvoscopiaIncubacaoAvesRequest();
            item.setNumero(ovo.numero());
            request.getItens().add(item);
        });
        return request;
    }

    @Transactional(readOnly = true)
    public FichaOvoscopiaIncubacaoAves ficha(Long incubacaoId) {
        IncubacaoAves incubacao = incubacaoRepository.findById(incubacaoId)
                .orElseThrow(() -> new AvesOperacaoException("INCUBACAO_NAO_ENCONTRADA",
                        "Incubação não encontrada.", HttpStatus.NOT_FOUND));
        LocalDate hoje = LocalDate.now(clock);
        return new FichaOvoscopiaIncubacaoAves("Sítio Guaratinguetá", incubacao.getCodigo(),
                incubacao.getMetodo(), incubacao.getDataInicio(), diaIncubacao(incubacao, hoje),
                hoje, hoje.plusDays(properties.getTarefaOvoscopiaDias()), ovos(incubacaoId));
    }

    private void validar(IncubacaoAves incubacao, RegistrarOvoscopiaIncubacaoAvesRequest request) {
        if (request.getDataOvoscopia() == null || request.getDataOvoscopia().isBefore(incubacao.getDataInicio())) {
            throw new AvesOperacaoException("OVOSCOPIA_DATA_INVALIDA",
                    "A data da ovoscopia deve ser igual ou posterior ao início da incubação.");
        }
        if (request.getItens() == null || request.getItens().isEmpty()) {
            throw new AvesOperacaoException("OVOSCOPIA_SEM_OVOS", "Informe pelo menos um ovo avaliado.");
        }
        LinkedHashSet<Integer> numeros = new LinkedHashSet<>();
        for (ItemOvoscopiaIncubacaoAvesRequest item : request.getItens()) {
            if (item.getNumero() == null || item.getNumero() < 1
                    || item.getNumero() > incubacao.getQuantidadeOvos()) {
                throw new AvesOperacaoException("OVO_INVALIDO", "Ovo informado não pertence à incubação.");
            }
            if (!numeros.add(item.getNumero())) {
                throw new AvesOperacaoException("OVO_DUPLICADO", "Cada ovo deve aparecer uma única vez por ovoscopia.");
            }
            if (item.getAchado() == null) {
                throw new AvesOperacaoException("ACHADO_OBRIGATORIO", "Informe o achado de cada ovo avaliado.");
            }
        }
        if (request.getProximaVerificacao() != null
                && request.getProximaVerificacao().isBefore(request.getDataOvoscopia())) {
            throw new AvesOperacaoException("PROXIMA_OVOSCOPIA_INVALIDA",
                    "A próxima verificação não pode ser anterior à ovoscopia atual.");
        }
    }

    private Map<Long, ItemOvoscopiaIncubacaoAves> ultimosPorOvo(Long incubacaoId) {
        Map<Long, ItemOvoscopiaIncubacaoAves> ultimos = new LinkedHashMap<>();
        List<OvoscopiaIncubacaoAves> historico = ovoscopiaRepository
                .findByIncubacaoIdOrderByDataOvoscopiaDescIdDesc(incubacaoId);
        historico.stream()
                .sorted(Comparator.comparing(OvoscopiaIncubacaoAves::getDataOvoscopia)
                        .thenComparing(OvoscopiaIncubacaoAves::getId))
                .flatMap(ovoscopia -> ovoscopia.getItens().stream())
                .forEach(item -> ultimos.put(item.getOvo().getId(), item));
        return ultimos;
    }

    private OvoIncubacaoAvesResumo resumo(OvoIncubacaoAves ovo, ItemOvoscopiaIncubacaoAves ultimo) {
        return new OvoIncubacaoAvesResumo(ovo.getId(), ovo.getNumero(), rotulo(ovo.getNumero()),
                ultimo == null ? null : ultimo.getAchado(),
                ultimo == null ? null : ultimo.getOvoscopia().getDataOvoscopia(),
                ultimo == null ? null : ultimo.getObservacao(),
                ultimo != null && ultimo.getAchado() == AchadoOvoscopiaAves.REAVALIAR);
    }

    private OvoscopiaIncubacaoAvesResumo resumo(OvoscopiaIncubacaoAves ovoscopia) {
        return new OvoscopiaIncubacaoAvesResumo(ovoscopia.getId(), ovoscopia.getDataOvoscopia(),
                ovoscopia.getDiaIncubacao(), ovoscopia.getProximaVerificacao(), ovoscopia.getResponsavel(),
                ovoscopia.getItens().size(),
                contar(ovoscopia, AchadoOvoscopiaAves.DESENVOLVIMENTO_VISIVEL),
                contar(ovoscopia, AchadoOvoscopiaAves.RACHADURA),
                contar(ovoscopia, AchadoOvoscopiaAves.REAVALIAR),
                contar(ovoscopia, AchadoOvoscopiaAves.PERDA_RETIRADA),
                ovoscopia.getObservacaoGeral());
    }

    private int contar(OvoscopiaIncubacaoAves ovoscopia, AchadoOvoscopiaAves achado) {
        return (int) ovoscopia.getItens().stream().filter(item -> item.getAchado() == achado).count();
    }

    private void sincronizarTarefaReavaliacao(IncubacaoAves incubacao, OvoscopiaIncubacaoAves ovoscopia,
            UsuarioAtor ator) {
        tarefaService.sincronizarAutomatica(new TarefaAutomaticaRequest(
                "CRIACAO:AVES:INCUBACAO:" + incubacao.getId() + ":OVOSCOPIA_REAVALIACAO",
                "Reavaliar ovos de " + incubacao.getCodigo(),
                "Há ovos marcados para reavaliação na ovoscopia operacional.",
                PrioridadeTarefa.ALTA,
                LocalDateTime.of(ovoscopia.getProximaVerificacao(), LocalTime.of(8, 0)),
                ModuloOrigem.CRIACOES,
                "INCUBACAO:" + incubacao.getId()), ator);
    }

    private LocalDate proximaVerificacao(IncubacaoAves incubacao, RegistrarOvoscopiaIncubacaoAvesRequest request) {
        LocalDate proxima = request.getProximaVerificacao() == null
                ? request.getDataOvoscopia().plusDays(properties.getTarefaOvoscopiaDias())
                : request.getProximaVerificacao();
        return proxima.isAfter(incubacao.getDataPrevistaEclosao()) ? incubacao.getDataPrevistaEclosao() : proxima;
    }

    private int diaIncubacao(IncubacaoAves incubacao, LocalDate data) {
        return (int) Math.max(1, ChronoUnit.DAYS.between(incubacao.getDataInicio(), data) + 1);
    }

    private String rotulo(int numero) {
        return "Ovo " + String.format("%02d", numero);
    }

    private String texto(String valor) {
        return StringUtils.hasText(valor) ? valor.trim() : null;
    }

    private String obrigatorio(String valor, String campo) {
        if (!StringUtils.hasText(valor)) {
            throw new AvesOperacaoException("CAMPO_OBRIGATORIO", campo + " é obrigatória.");
        }
        return valor.trim();
    }

    private AvesOperacaoException conflito(String codigo, String mensagem) {
        return new AvesOperacaoException(codigo, mensagem, HttpStatus.CONFLICT);
    }
}

