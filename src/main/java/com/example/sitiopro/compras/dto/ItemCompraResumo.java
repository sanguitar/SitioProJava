package com.example.sitiopro.compras.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ItemCompraResumo(
        Long id,
        Long itemEstoqueId,
        String itemNome,
        String unidade,
        BigDecimal quantidade,
        BigDecimal custoUnitario,
        BigDecimal subtotal,
        Long localDestinoId,
        String localDestinoNome,
        String loteCodigo,
        LocalDate validade,
        Long movimentoEstoqueId) {
}
