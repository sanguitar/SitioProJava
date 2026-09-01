package com.example.sitiopro.tarefas.dto;

import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;

import java.time.LocalDateTime;

public record TarefaAutomaticaRequest(
        String chaveAutomacao,
        String titulo,
        String descricao,
        PrioridadeTarefa prioridade,
        LocalDateTime dataVencimento,
        ModuloOrigem moduloOrigem,
        String referenciaOrigem) {
}
