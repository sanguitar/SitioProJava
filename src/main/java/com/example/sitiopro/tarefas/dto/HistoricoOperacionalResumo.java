package com.example.sitiopro.tarefas.dto;

import com.example.sitiopro.tarefas.entity.TipoEventoOperacional;

import java.time.LocalDateTime;

public record HistoricoOperacionalResumo(
        Long id,
        TipoEventoOperacional tipo,
        LocalDateTime ocorridoEm,
        String ator,
        String detalhe) {
}
