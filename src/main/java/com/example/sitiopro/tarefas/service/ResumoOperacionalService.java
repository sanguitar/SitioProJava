package com.example.sitiopro.tarefas.service;

import com.example.sitiopro.tarefas.dto.TarefaResumoOperacional;
import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;
import com.example.sitiopro.tarefas.entity.SeveridadeAlerta;
import com.example.sitiopro.tarefas.entity.StatusAlerta;
import com.example.sitiopro.tarefas.entity.StatusTarefa;
import com.example.sitiopro.tarefas.repository.AlertaRepository;
import com.example.sitiopro.tarefas.repository.TarefaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResumoOperacionalService {

    private static final List<StatusTarefa> TAREFAS_ABERTAS = List.of(
            StatusTarefa.PENDENTE, StatusTarefa.EM_ANDAMENTO);
    private static final List<StatusAlerta> ALERTAS_ABERTOS = List.of(
            StatusAlerta.ATIVO, StatusAlerta.RECONHECIDO);

    private final TarefaRepository tarefaRepository;
    private final AlertaRepository alertaRepository;
    private final Clock clock;

    public ResumoOperacionalService(TarefaRepository tarefaRepository, AlertaRepository alertaRepository,
            Clock clock) {
        this.tarefaRepository = tarefaRepository;
        this.alertaRepository = alertaRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public TarefaResumoOperacional resumo() {
        LocalDate hoje = LocalDate.now(clock);
        LocalDateTime agora = LocalDateTime.now(clock);
        return new TarefaResumoOperacional(
                tarefaRepository.countByStatusInAndDataVencimentoGreaterThanEqualAndDataVencimentoLessThan(
                        TAREFAS_ABERTAS, hoje.atStartOfDay(), hoje.plusDays(1).atStartOfDay()),
                tarefaRepository.countByStatusInAndDataVencimentoBefore(TAREFAS_ABERTAS, agora),
                tarefaRepository.countByStatusInAndPrioridade(TAREFAS_ABERTAS, PrioridadeTarefa.CRITICA),
                alertaRepository.countByStatusIn(ALERTAS_ABERTOS),
                alertaRepository.countByStatusInAndSeveridade(ALERTAS_ABERTOS, SeveridadeAlerta.CRITICA));
    }
}
