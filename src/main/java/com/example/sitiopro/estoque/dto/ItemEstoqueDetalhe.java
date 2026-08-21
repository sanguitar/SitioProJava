package com.example.sitiopro.estoque.dto;

import java.util.List;

public record ItemEstoqueDetalhe(
        ItemEstoqueResumo resumo,
        String descricao,
        boolean controlaLote,
        boolean controlaValidade,
        List<SaldoLocalEstoque> saldosPorLocal,
        List<MovimentoEstoqueResponse> movimentacoes) {
}
