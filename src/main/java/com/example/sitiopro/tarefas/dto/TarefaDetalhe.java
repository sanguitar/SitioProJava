package com.example.sitiopro.tarefas.dto;

import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.OrigemTarefa;
import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;
import com.example.sitiopro.tarefas.entity.StatusTarefa;
import com.example.sitiopro.tarefas.entity.TipoRecorrencia;

import java.time.LocalDateTime;
import java.util.List;

public record TarefaDetalhe(
        Long id,
        String titulo,
        String descricao,
        StatusTarefa status,
        PrioridadeTarefa prioridade,
        LocalDateTime dataCriacao,
        LocalDateTime dataInicio,
        LocalDateTime dataVencimento,
        LocalDateTime dataConclusao,
        Long responsavelId,
        String responsavelNome,
        Long criadoPorId,
        String criadoPorNome,
        OrigemTarefa origem,
        ModuloOrigem moduloOrigem,
        String referenciaOrigem,
        TipoRecorrencia recorrencia,
        Integer intervaloDias,
        LocalDateTime proximaOcorrenciaEm,
        boolean recorrenciaAtiva,
        boolean ativo,
        long versao,
        boolean vencida,
        List<HistoricoOperacionalResumo> historico) {
}
