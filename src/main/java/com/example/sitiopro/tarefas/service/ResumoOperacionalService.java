package com.example.sitiopro.tarefas.service;

import com.example.sitiopro.tarefas.dto.TarefaResumoOperacional;
import com.example.sitiopro.tarefas.dto.TarefaResumo;
import com.example.sitiopro.tarefas.dto.AlertaResumo;
import com.example.sitiopro.tarefas.dto.TarefasAlertasPainelResumo;
import com.example.sitiopro.tarefas.entity.StatusAlerta;
import com.example.sitiopro.tarefas.entity.StatusTarefa;
import com.example.sitiopro.tarefas.repository.AlertaRepository;
import com.example.sitiopro.tarefas.repository.TarefaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;

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
        TarefaRepository.PainelContadores tarefas = tarefaRepository.contarParaPainel(
                TAREFAS_ABERTAS, hoje.atStartOfDay(), hoje.plusDays(1).atStartOfDay(), agora);
        AlertaRepository.PainelContadores alertas = alertaRepository.contarParaPainel(ALERTAS_ABERTOS);
        return new TarefaResumoOperacional(
                valor(tarefas.getPendentesHoje()),
                valor(tarefas.getVencidas()),
                valor(tarefas.getCriticas()),
                valor(alertas.getAtivos()),
                valor(alertas.getCriticos()));
    }

    @Transactional(readOnly = true)
    public TarefasAlertasPainelResumo resumoPainel(int limite) {
        LocalDate hoje = LocalDate.now(clock);
        LocalDateTime agora = LocalDateTime.now(clock);
        LocalDateTime inicioHoje = hoje.atStartOfDay();
        LocalDateTime fimHoje = hoje.plusDays(1).atStartOfDay();
        int tamanho = Math.max(1, Math.min(limite, 10));

        TarefaRepository.PainelContadores tarefas = tarefaRepository.contarParaPainel(
                TAREFAS_ABERTAS, inicioHoje, fimHoje, agora);
        AlertaRepository.PainelContadores alertas = alertaRepository.contarParaPainel(ALERTAS_ABERTOS);
        List<TarefaResumo> tarefasDestaque = tarefaRepository.buscarDestaquesPainel(
                        TAREFAS_ABERTAS, agora, inicioHoje, fimHoje, PageRequest.of(0, tamanho)).stream()
                .map(tarefa -> new TarefaResumo(
                        tarefa.getId(), tarefa.getTitulo(), tarefa.getStatus(), tarefa.getPrioridade(),
                        tarefa.getDataVencimento(),
                        tarefa.getResponsavel() == null ? null : tarefa.getResponsavel().getId(),
                        tarefa.getResponsavel() == null ? null : tarefa.getResponsavel().getNome(),
                        tarefa.getOrigem(), tarefa.getModuloOrigem(),
                        tarefa.getDataVencimento() != null && tarefa.getDataVencimento().isBefore(agora)))
                .toList();
        List<AlertaResumo> alertasDestaque = alertaRepository
                .buscarDestaquesPainel(ALERTAS_ABERTOS, PageRequest.of(0, tamanho)).stream()
                .map(alerta -> new AlertaResumo(
                        alerta.getId(), alerta.getTitulo(), alerta.getSeveridade(), alerta.getStatus(),
                        alerta.getModuloOrigem(), alerta.getTipo(), alerta.getReferenciaOrigem(),
                        alerta.getDetectadoEm(), alerta.getAtualizadoEm(),
                        alerta.getTarefa() == null ? null : alerta.getTarefa().getId()))
                .toList();

        return new TarefasAlertasPainelResumo(
                new TarefasAlertasPainelResumo.Tarefas(
                        valor(tarefas.getAbertas()), valor(tarefas.getPendentesHoje()), valor(tarefas.getVencidas()),
                        valor(tarefas.getCriticas()), valor(tarefas.getEmAndamento()), tarefasDestaque),
                new TarefasAlertasPainelResumo.Alertas(
                        valor(alertas.getAtivos()), valor(alertas.getCriticos()),
                        valor(alertas.getAltaSeveridade()), alertasDestaque));
    }

    private long valor(Long valor) {
        return valor == null ? 0 : valor;
    }
}
