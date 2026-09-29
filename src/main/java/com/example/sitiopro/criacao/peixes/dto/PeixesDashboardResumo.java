package com.example.sitiopro.criacao.peixes.dto;
import java.math.BigDecimal;
public record PeixesDashboardResumo(long lotesAtivos,long quantidadeAtual,BigDecimal biomassaEstimada,
        BigDecimal pesoMedio,BigDecimal consumoRecente,long mortalidadeRecente,long tarefasAbertas,long alertasAbertos) {}
