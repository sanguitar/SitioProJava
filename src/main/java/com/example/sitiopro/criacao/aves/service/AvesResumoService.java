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
import com.example.sitiopro.tarefas.entity.StatusAlerta;
import com.example.sitiopro.tarefas.repository.AlertaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AvesResumoService {
    private final LoteAvesRepository loteRepository;
    private final IncubacaoAvesRepository incubacaoRepository;
    private final RegistroPosturaAvesRepository posturaRepository;
    private final MortalidadeAvesRepository mortalidadeRepository;
    private final AlertaRepository alertaRepository;
    private final AvesProperties properties;
    private final Clock clock;

    public AvesResumoService(LoteAvesRepository loteRepository, IncubacaoAvesRepository incubacaoRepository,
            RegistroPosturaAvesRepository posturaRepository, MortalidadeAvesRepository mortalidadeRepository,
            AlertaRepository alertaRepository, AvesProperties properties, Clock clock) {
        this.loteRepository = loteRepository; this.incubacaoRepository = incubacaoRepository;
        this.posturaRepository = posturaRepository; this.mortalidadeRepository = mortalidadeRepository;
        this.alertaRepository = alertaRepository; this.properties = properties; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AvesResumo resumo() {
        LocalDate hoje = LocalDate.now(clock);
        return new AvesResumo(loteRepository.countByStatus(StatusLoteAves.ATIVO),
                loteRepository.somarQuantidadePorStatus(StatusLoteAves.ATIVO),
                incubacaoRepository.countByStatus(StatusIncubacaoAves.EM_INCUBACAO),
                incubacaoRepository.countByStatusAndDataPrevistaEclosaoBetween(StatusIncubacaoAves.EM_INCUBACAO,
                        hoje, hoje.plusDays(properties.getEclosaoProximaDias())),
                alertaRepository.countByModuloOrigemAndStatusIn(ModuloOrigem.CRIACOES,
                        List.of(StatusAlerta.ATIVO, StatusAlerta.RECONHECIDO)),
                posturaRepository.somarInteirosNaData(hoje),
                mortalidadeRepository.somarTotalDesde(LocalDateTime.now(clock).minusDays(properties.getMortalidadePeriodoDias())),
                LocalDateTime.now(clock));
    }
}
