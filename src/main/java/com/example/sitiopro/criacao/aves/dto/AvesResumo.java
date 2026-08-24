package com.example.sitiopro.criacao.aves.dto;

import java.time.LocalDateTime;

public record AvesResumo(long lotesAtivos, long totalAves, long incubacoesAtivas, long eclosoesProximas,
        long alertasAtivos, long ovosHoje, long mortalidadeRecente, LocalDateTime geradoEm) {
}
