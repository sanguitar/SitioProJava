package com.example.sitiopro.criacao.aves.dto;

import java.time.LocalDate;

public record OvoscopiaIncubacaoAvesResumo(
        Long id,
        LocalDate dataOvoscopia,
        int diaIncubacao,
        LocalDate proximaVerificacao,
        String responsavel,
        int totalAvaliado,
        int desenvolvimentoVisivel,
        int rachaduras,
        int reavaliar,
        int retirados,
        String observacaoGeral) {
}

