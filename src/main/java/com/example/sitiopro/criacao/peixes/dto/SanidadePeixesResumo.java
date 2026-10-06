package com.example.sitiopro.criacao.peixes.dto;

public record SanidadePeixesResumo(long registrosUltimos30Dias, long proximasAcoes,
        long acoesPendentes) {
    public static SanidadePeixesResumo vazio() { return new SanidadePeixesResumo(0, 0, 0); }
}
