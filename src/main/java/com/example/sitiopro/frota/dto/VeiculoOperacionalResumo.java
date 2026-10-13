package com.example.sitiopro.frota.dto;

import java.math.BigDecimal;

public record VeiculoOperacionalResumo(
        VeiculoResumo veiculo,
        long abastecimentos,
        BigDecimal litros,
        BigDecimal custoTotal,
        BigDecimal consumoMedio,
        String unidadeConsumo) {
}
