package com.example.sitiopro.criacao.aves.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AvesResumo(
        long lotesAtivos,
        long totalAves,
        long incubacoesAtivas,
        long eclosoesProximas,
        long alertasAtivos,
        long ovosHoje,
        long mortalidadeRecente,
        LocalDateTime geradoEm,
        long ovosEmIncubacao,
        String proximaIncubacaoCodigo,
        LocalDate proximaEclosao,
        Long diasProximaEclosao,
        long pintinhosRecentes) {

    public AvesResumo(long lotesAtivos, long totalAves, long incubacoesAtivas, long eclosoesProximas,
            long alertasAtivos, long ovosHoje, long mortalidadeRecente, LocalDateTime geradoEm) {
        this(lotesAtivos, totalAves, incubacoesAtivas, eclosoesProximas, alertasAtivos,
                ovosHoje, mortalidadeRecente, geradoEm, 0, null, null, null, 0);
    }
}
