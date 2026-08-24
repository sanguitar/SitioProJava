package com.example.sitiopro.tarefas.dto;

import java.util.List;

public record TarefasAlertasPainelResumo(
        Tarefas tarefas,
        Alertas alertas) {

    public record Tarefas(
            long pendentesHoje,
            long vencidas,
            long criticas,
            long emAndamento,
            List<TarefaResumo> destaques) {
    }

    public record Alertas(
            long ativos,
            long criticos,
            long altaSeveridade,
            List<AlertaResumo> destaques) {
    }
}
