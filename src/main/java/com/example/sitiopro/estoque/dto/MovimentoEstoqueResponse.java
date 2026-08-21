package com.example.sitiopro.estoque.dto;

import com.example.sitiopro.estoque.entity.TipoMovimentoEstoque;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimentoEstoqueResponse(
        Long id,
        Long itemId,
        String itemNome,
        TipoMovimentoEstoque tipo,
        String tipoRotulo,
        BigDecimal quantidade,
        String unidade,
        String localOrigem,
        String localDestino,
        String loteCodigo,
        BigDecimal custoUnitario,
        BigDecimal custoTotal,
        String observacao,
        LocalDateTime dataMovimento,
        String criadoPor) {
}
