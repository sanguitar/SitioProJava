package com.example.sitiopro.estoque.dto;

import java.math.BigDecimal;
import java.util.List;

public record EstoqueDashboardResumo(
        long itensCadastrados,
        long itensAbaixoMinimo,
        long lotesVencendo,
        BigDecimal valorEstimado,
        List<ItemEstoqueResumo> itensCriticos,
        List<LoteEstoqueResumo> lotesProximos,
        List<MovimentoEstoqueResponse> movimentacoesRecentes) {
}
