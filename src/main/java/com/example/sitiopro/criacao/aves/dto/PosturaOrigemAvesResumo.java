package com.example.sitiopro.criacao.aves.dto;

import java.time.LocalDate;

public record PosturaOrigemAvesResumo(Long id, Long loteId, String loteCodigo,
        LocalDate dataColeta, int ovosInteiros) {
}

