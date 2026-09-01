package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.criacao.aves.config.AvesProperties;
import com.example.sitiopro.criacao.aves.dto.AvesResumo;
import com.example.sitiopro.criacao.aves.entity.StatusIncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.StatusLoteAves;
import com.example.sitiopro.criacao.aves.repository.IncubacaoAvesRepository;
import com.example.sitiopro.criacao.aves.repository.LoteAvesRepository;
import com.example.sitiopro.criacao.aves.repository.MortalidadeAvesRepository;
import com.example.sitiopro.criacao.aves.repository.RegistroPosturaAvesRepository;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.service.AlertaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class AvesResumoService {
    private final LoteAvesRepository loteRepository;
    private final IncubacaoAvesRepository incubacaoRepository;
    private final RegistroPosturaAvesRepository posturaRepository;
    private final MortalidadeAvesRepository mortalidadeRepository;
    private final AlertaService alertaService;
    private final AvesProperties properties;
    private final Clock clock;

    public AvesResumoService(LoteAvesRepository loteRepository, IncubacaoAvesRepository incubacaoRepository,
            RegistroPosturaAvesRepository posturaRepository, MortalidadeAvesRepository mortalidadeRepository,
            AlertaService alertaService, AvesProperties properties, Clock clock) {
        this.loteRepository = loteRepository; this.incubacaoRepository = incubacaoRepository;
        this.posturaRepository = posturaRepository; this.mortalidadeRepository = mortalidadeRepository;
        this.alertaService = alertaService; this.properties = properties; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AvesResumo resumo() {
        LocalDate hoje = LocalDate.now(clock);
        var proxima = incubacaoRepository
                .findFirstByStatusOrderByDataPrevistaEclosaoAscIdAsc(StatusIncubacaoAves.EM_INCUBACAO)
                .orElse(null);
        return new AvesResumo(loteRepository.countByStatus(StatusLoteAves.ATIVO),
                loteRepository.somarQuantidadePorStatus(StatusLoteAves.ATIVO),
                incubacaoRepository.countByStatus(StatusIncubacaoAves.EM_INCUBACAO),
                incubacaoRepository.countByStatusAndDataPrevistaEclosaoBetween(StatusIncubacaoAves.EM_INCUBACAO,
                        hoje, hoje.plusDays(properties.getEclosaoProximaDias())),
                alertaService.contarAbertos(ModuloOrigem.CRIACOES),
                posturaRepository.somarInteirosNaData(hoje),
                mortalidadeRepository.somarTotalDesde(LocalDateTime.now(clock).minusDays(properties.getMortalidadePeriodoDias())),
                LocalDateTime.now(clock),
                incubacaoRepository.somarOvosPorStatus(StatusIncubacaoAves.EM_INCUBACAO),
                proxima == null ? null : proxima.getCodigo(),
                proxima == null ? null : proxima.getDataPrevistaEclosao(),
                proxima == null ? null : ChronoUnit.DAYS.between(hoje, proxima.getDataPrevistaEclosao()),
                incubacaoRepository.somarPintinhosDesde(StatusIncubacaoAves.FINALIZADA,
                        hoje.minusDays(properties.getPintinhosRecentesDias())));
    }
}
