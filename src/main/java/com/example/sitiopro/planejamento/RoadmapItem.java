package com.example.sitiopro.planejamento;

public record RoadmapItem(
        String grupo,
        String titulo,
        String rota,
        StatusPlanejamento status,
        String descricao) {
}
