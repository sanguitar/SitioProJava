package com.example.sitiopro.tarefas.dto;

public record TarefaResumoOperacional(
        long tarefasPendentesHoje,
        long tarefasVencidas,
        long tarefasCriticas,
        long alertasAtivos,
        long alertasCriticos) {
}
