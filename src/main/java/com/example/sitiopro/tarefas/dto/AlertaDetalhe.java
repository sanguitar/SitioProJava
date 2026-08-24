package com.example.sitiopro.tarefas.dto;

import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.SeveridadeAlerta;
import com.example.sitiopro.tarefas.entity.StatusAlerta;
import com.example.sitiopro.tarefas.entity.TipoAlerta;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record AlertaDetalhe(
        Long id,
        String titulo,
        String descricao,
        SeveridadeAlerta severidade,
        StatusAlerta status,
        ModuloOrigem moduloOrigem,
        TipoAlerta tipo,
        String referenciaOrigem,
        String chaveDeduplicacao,
        LocalDateTime detectadoEm,
        LocalDateTime atualizadoEm,
        LocalDateTime reconhecidoEm,
        String reconhecidoPorNome,
        LocalDateTime resolvidoEm,
        Map<String, Object> contexto,
        Long tarefaId,
        long versao,
        List<HistoricoOperacionalResumo> historico) {
}
