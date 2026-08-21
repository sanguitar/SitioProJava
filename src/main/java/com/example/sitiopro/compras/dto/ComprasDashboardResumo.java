package com.example.sitiopro.compras.dto;

import java.math.BigDecimal;
import java.util.List;

public record ComprasDashboardResumo(
        long comprasConfirmadasNoMes,
        BigDecimal valorConfirmadoNoMes,
        long rascunhos,
        long fornecedoresAtivos,
        CompraResumo ultimaCompraConfirmada,
        List<CompraResumo> comprasRecentes) {
}
