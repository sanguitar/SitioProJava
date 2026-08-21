package com.example.sitiopro.compras.dto;

import com.example.sitiopro.compras.entity.StatusCompra;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CompraResumo(
        Long id,
        Long fornecedorId,
        String fornecedorNome,
        LocalDate dataCompra,
        String numeroDocumento,
        StatusCompra status,
        String statusRotulo,
        BigDecimal subtotal,
        BigDecimal frete,
        BigDecimal desconto,
        BigDecimal total,
        int quantidadeItens,
        LocalDateTime confirmadoEm,
        String confirmadoPor) {
}
