package com.example.sitiopro.estoque.dto;

import java.math.BigDecimal;

public record ItemEstoqueResumo(
        Long id,
        String nome,
        String categoria,
        String unidade,
        BigDecimal saldo,
        BigDecimal estoqueMinimo,
        boolean ativo,
        boolean estoqueBaixo,
        BigDecimal ultimoPreco,
        BigDecimal custoMedio) {
}
