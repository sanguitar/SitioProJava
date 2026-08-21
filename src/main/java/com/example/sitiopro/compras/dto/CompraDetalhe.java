package com.example.sitiopro.compras.dto;

import com.example.sitiopro.compras.entity.StatusCompra;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CompraDetalhe(
        Long id,
        FornecedorResumo fornecedor,
        LocalDate dataCompra,
        String numeroDocumento,
        String observacao,
        StatusCompra status,
        String statusRotulo,
        BigDecimal subtotal,
        BigDecimal frete,
        BigDecimal desconto,
        BigDecimal total,
        List<ItemCompraResumo> itens,
        LocalDateTime confirmadoEm,
        String confirmadoPor,
        LocalDateTime criadoEm,
        String criadoPor,
        LocalDateTime alteradoEm,
        String alteradoPor) {

    public boolean rascunho() {
        return status == StatusCompra.RASCUNHO;
    }
}
