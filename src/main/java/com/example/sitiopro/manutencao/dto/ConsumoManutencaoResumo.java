package com.example.sitiopro.manutencao.dto;

import java.math.BigDecimal;

public record ConsumoManutencaoResumo(Long id, Long itemId, String itemNome, Long localId,
        String localNome, BigDecimal quantidade, String unidade, String loteEstoqueCodigo,
        Long movimentoEstoqueId) {}
