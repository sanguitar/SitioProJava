package com.example.sitiopro.tarefas.dto;

import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.SeveridadeAlerta;
import com.example.sitiopro.tarefas.entity.StatusAlerta;
import com.example.sitiopro.tarefas.entity.TipoAlerta;

import java.time.LocalDateTime;

public record AlertaResumo(
        Long id,
        String titulo,
        SeveridadeAlerta severidade,
        StatusAlerta status,
        ModuloOrigem moduloOrigem,
        TipoAlerta tipo,
        String referenciaOrigem,
        LocalDateTime detectadoEm,
        LocalDateTime atualizadoEm,
        Long tarefaId) {
}
