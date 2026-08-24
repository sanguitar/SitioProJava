package com.example.sitiopro.tarefas.dto;

import com.example.sitiopro.tarefas.entity.SeveridadeAlerta;

import java.util.Map;

public record CondicaoAlerta(
        String chaveDeduplicacao,
        String titulo,
        String descricao,
        SeveridadeAlerta severidade,
        String referenciaOrigem,
        Map<String, Object> contexto) {

    public CondicaoAlerta {
        contexto = contexto == null ? Map.of() : Map.copyOf(contexto);
    }
}
