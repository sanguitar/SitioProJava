package com.example.sitiopro.estoque.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoteEstoqueResumo(
        Long id,
        String itemNome,
        String codigo,
        LocalDate validade,
        BigDecimal saldo,
        String unidade) {
}
