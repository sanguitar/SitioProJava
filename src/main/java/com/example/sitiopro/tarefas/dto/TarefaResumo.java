package com.example.sitiopro.tarefas.dto;

import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.OrigemTarefa;
import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;
import com.example.sitiopro.tarefas.entity.StatusTarefa;

import java.time.LocalDateTime;

public record TarefaResumo(
        Long id,
        String titulo,
        StatusTarefa status,
        PrioridadeTarefa prioridade,
        LocalDateTime dataVencimento,
        Long responsavelId,
        String responsavelNome,
        OrigemTarefa origem,
        ModuloOrigem moduloOrigem,
        boolean vencida) {
}
