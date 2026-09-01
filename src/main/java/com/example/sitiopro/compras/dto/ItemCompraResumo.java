package com.example.sitiopro.compras.dto;

import com.example.sitiopro.compras.entity.TipoEmbalagem;

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
        BigDecimal quantidadeVolumes,
        TipoEmbalagem tipoEmbalagem,
        String tipoEmbalagemRotulo,
        BigDecimal conteudoPorVolume,
        BigDecimal precoPorVolume,
        String unidadeBase,
        BigDecimal quantidadeEstoque,
        BigDecimal valorTotal,
        boolean apresentacaoComercial,
        Long localDestinoId,
        String localDestinoNome,
        String loteCodigo,
        LocalDate validade,
        Long movimentoEstoqueId) {
}
