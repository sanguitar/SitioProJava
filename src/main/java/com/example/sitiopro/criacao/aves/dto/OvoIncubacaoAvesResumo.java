package com.example.sitiopro.criacao.aves.dto;

import com.example.sitiopro.criacao.aves.entity.AchadoOvoscopiaAves;

import java.time.LocalDate;

public record OvoIncubacaoAvesResumo(
        Long id,
        int numero,
        String rotulo,
        AchadoOvoscopiaAves situacaoAtual,
        LocalDate ultimaOvoscopiaEm,
        String ultimaObservacao,
        boolean pendenteReavaliacao) {
}

