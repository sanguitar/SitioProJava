package com.example.sitiopro.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DashboardTendenciasResumo(
        PosturaSerie postura7Dias,
        List<ConsumoSerie> consumoRacao7Dias,
        ComprasSerie compras6Meses) {

    public record PosturaSerie(
            long totalOvos,
            boolean temDados,
            List<PosturaPonto> pontos) {
    }

    public record PosturaPonto(
            LocalDate data,
            String rotulo,
            long inteiros,
            long perdas,
            long total,
            int percentualEscala,
            int percentualInteiros) {
    }

    public record ConsumoSerie(
            String unidade,
            BigDecimal total,
            boolean temDados,
            List<ValorPonto> pontos) {
    }

    public record ComprasSerie(
            BigDecimal total,
            boolean temDados,
            List<ValorPonto> pontos) {
    }

    public record ValorPonto(
            String chave,
            String rotulo,
            BigDecimal valor,
            int percentualEscala) {
    }
}
