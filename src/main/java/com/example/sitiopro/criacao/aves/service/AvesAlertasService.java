package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalService;
import com.example.sitiopro.criacao.aves.config.AvesProperties;
import com.example.sitiopro.criacao.aves.entity.*;
import com.example.sitiopro.criacao.aves.repository.IncubacaoAvesRepository;
import com.example.sitiopro.criacao.aves.repository.LoteAvesRepository;
import com.example.sitiopro.criacao.aves.repository.MortalidadeAvesRepository;
import com.example.sitiopro.tarefas.dto.CondicaoAlerta;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.SeveridadeAlerta;
import com.example.sitiopro.tarefas.entity.TipoAlerta;
import com.example.sitiopro.tarefas.service.AlertaService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AvesAlertasService {
    private final LoteAvesRepository loteRepository;
    private final MortalidadeAvesRepository mortalidadeRepository;
    private final IncubacaoAvesRepository incubacaoRepository;
    private final AlertaService alertaService;
    private final AvesProperties properties;
    private final ConfiguracaoOperacionalService configuracaoOperacionalService;
    private final Clock clock;

    public AvesAlertasService(LoteAvesRepository loteRepository, MortalidadeAvesRepository mortalidadeRepository,
            IncubacaoAvesRepository incubacaoRepository, AlertaService alertaService,
            AvesProperties properties, ConfiguracaoOperacionalService configuracaoOperacionalService, Clock clock) {
        this.loteRepository = loteRepository;
        this.mortalidadeRepository = mortalidadeRepository;
        this.incubacaoRepository = incubacaoRepository;
        this.alertaService = alertaService;
        this.properties = properties;
        this.configuracaoOperacionalService = configuracaoOperacionalService;
        this.clock = clock;
    }

    public void avaliar() {
        avaliarMortalidade();
        avaliarEclosoesProximas();
        avaliarIncubacoesAtrasadas();
    }

    private void avaliarMortalidade() {
        LocalDateTime desde = LocalDateTime.now(clock).minusDays(properties.getMortalidadePeriodoDias());
        List<CondicaoAlerta> condicoes = loteRepository.findByStatusOrderByCodigoAsc(StatusLoteAves.ATIVO).stream()
                .map(lote -> mortalidade(lote, desde))
                .filter(java.util.Objects::nonNull)
                .toList();
        alertaService.sincronizar(ModuloOrigem.CRIACOES, TipoAlerta.CRIACAO_MORTALIDADE_ALTA, condicoes);
    }

    private CondicaoAlerta mortalidade(LoteAves lote, LocalDateTime desde) {
        long quantidade = mortalidadeRepository.somarDesde(lote.getId(), desde);
        BigDecimal percentual = BigDecimal.valueOf(quantidade).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(lote.getQuantidadeInicial()), 2, RoundingMode.HALF_UP);
        if (percentual.compareTo(properties.getMortalidadeAlertaPercentual()) < 0) return null;
        return new CondicaoAlerta("CRIACAO:AVES:LOTE:" + lote.getId() + ":MORTALIDADE_ALTA",
                "Mortalidade elevada no lote " + lote.getCodigo(),
                quantidade + " aves registradas em " + properties.getMortalidadePeriodoDias()
                        + " dias (" + percentual + "% do lote inicial).",
                percentual.compareTo(properties.getMortalidadeAlertaPercentual().multiply(BigDecimal.valueOf(2))) >= 0
                        ? SeveridadeAlerta.CRITICA : SeveridadeAlerta.ALTA,
                "LOTE:" + lote.getId(), Map.of("loteId", lote.getId(), "quantidade", quantidade,
                        "percentual", percentual));
    }

    private void avaliarEclosoesProximas() {
        LocalDate hoje = LocalDate.now(clock);
        LocalDate limite = hoje.plusDays(
                configuracaoOperacionalService.obter().antecedenciaAlertaEclosaoDias());
        List<CondicaoAlerta> condicoes = incubacaoRepository
                .findByStatusOrderByDataPrevistaEclosaoAsc(StatusIncubacaoAves.EM_INCUBACAO).stream()
                .filter(i -> !i.getDataPrevistaEclosao().isBefore(hoje) && !i.getDataPrevistaEclosao().isAfter(limite))
                .map(i -> new CondicaoAlerta("CRIACAO:AVES:INCUBACAO:" + i.getId() + ":ECLOSAO_PROXIMA",
                        "Eclosão próxima na incubação " + i.getCodigo(),
                        "Eclosão prevista para " + i.getDataPrevistaEclosao() + ".",
                        SeveridadeAlerta.ATENCAO, "INCUBACAO:" + i.getId(), Map.of("incubacaoId", i.getId())))
                .toList();
        alertaService.sincronizar(ModuloOrigem.CRIACOES, TipoAlerta.CRIACAO_INCUBACAO_ECLOSAO_PROXIMA, condicoes);
    }

    private void avaliarIncubacoesAtrasadas() {
        LocalDate hoje = LocalDate.now(clock);
        List<CondicaoAlerta> condicoes = incubacaoRepository
                .findByStatusOrderByDataPrevistaEclosaoAsc(StatusIncubacaoAves.EM_INCUBACAO).stream()
                .filter(i -> i.getDataPrevistaEclosao().isBefore(hoje))
                .map(i -> new CondicaoAlerta("CRIACAO:AVES:INCUBACAO:" + i.getId() + ":ATRASADA",
                        "Incubação " + i.getCodigo() + " ultrapassou a previsão",
                        "A eclosão estava prevista para " + i.getDataPrevistaEclosao() + " e a incubação continua aberta.",
                        SeveridadeAlerta.ALTA, "INCUBACAO:" + i.getId(), Map.of("incubacaoId", i.getId())))
                .toList();
        alertaService.sincronizar(ModuloOrigem.CRIACOES, TipoAlerta.CRIACAO_INCUBACAO_ATRASADA, condicoes);
    }
}
