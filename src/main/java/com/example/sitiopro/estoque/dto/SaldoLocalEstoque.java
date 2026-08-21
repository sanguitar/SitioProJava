package com.example.sitiopro.estoque.dto;

import java.math.BigDecimal;

public record SaldoLocalEstoque(
        Long localId,
        String localNome,
        BigDecimal saldo) {
}
