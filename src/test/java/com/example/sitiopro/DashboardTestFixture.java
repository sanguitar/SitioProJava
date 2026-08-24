package com.example.sitiopro;

import com.example.sitiopro.dashboard.dto.DashboardOperacionalResumo;
import com.example.sitiopro.dashboard.dto.EstadoClimaDashboard;
import com.example.sitiopro.dashboard.dto.NivelAtencao;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;
import com.example.sitiopro.tarefas.entity.SeveridadeAlerta;
import com.example.sitiopro.tarefas.entity.StatusAlerta;
import com.example.sitiopro.tarefas.entity.StatusTarefa;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class DashboardTestFixture {

    private DashboardTestFixture() {
    }

    public static DashboardOperacionalResumo vazio() {
        return new DashboardOperacionalResumo(
                NivelAtencao.NORMAL,
                "Operação sem pendências prioritárias neste momento.",
                new DashboardOperacionalResumo.TarefasResumo(0, 0, 0, 0, List.of()),
                new DashboardOperacionalResumo.AlertasResumo(0, 0, 0, List.of()),
                new DashboardOperacionalResumo.EstoqueResumo(0, 0, 0, List.of()),
                new DashboardOperacionalResumo.ComprasResumo(0, 0, BigDecimal.ZERO, null, List.of()),
                new DashboardOperacionalResumo.ClimaResumo(
                        EstadoClimaDashboard.SEM_DADOS, null, "Previsão ainda não sincronizada",
                        null, null, null, null),
                new DashboardOperacionalResumo.IntegracoesResumo(0, 0, 0, 0, 0, List.of()),
                LocalDateTime.of(2026, 8, 24, 8, 0));
    }

    public static DashboardOperacionalResumo comDestaques() {
        LocalDateTime agora = LocalDateTime.of(2026, 8, 24, 8, 0);
        return new DashboardOperacionalResumo(
                NivelAtencao.CRITICA,
                "Existem ocorrências críticas que precisam de atenção imediata.",
                new DashboardOperacionalResumo.TarefasResumo(1, 1, 1, 0, List.of(
                        new DashboardOperacionalResumo.TarefaItem(
                                10L, "Verificar irrigação", StatusTarefa.PENDENTE,
                                PrioridadeTarefa.CRITICA, agora.minusHours(1), "Operador", true))),
                new DashboardOperacionalResumo.AlertasResumo(1, 1, 0, List.of(
                        new DashboardOperacionalResumo.AlertaItem(
                                20L, "Integração indisponível", SeveridadeAlerta.CRITICA,
                                StatusAlerta.ATIVO, ModuloOrigem.INTEGRACOES, agora.minusMinutes(15)))),
                new DashboardOperacionalResumo.EstoqueResumo(0, 0, 0, List.of()),
                new DashboardOperacionalResumo.ComprasResumo(0, 0, BigDecimal.ZERO, null, List.of()),
                new DashboardOperacionalResumo.ClimaResumo(
                        EstadoClimaDashboard.SEM_DADOS, null, "Previsão ainda não sincronizada",
                        null, null, null, null),
                new DashboardOperacionalResumo.IntegracoesResumo(0, 0, 0, 0, 0, List.of()),
                agora);
    }
}
