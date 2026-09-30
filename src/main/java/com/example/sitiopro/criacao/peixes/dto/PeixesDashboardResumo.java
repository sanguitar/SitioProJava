package com.example.sitiopro.criacao.peixes.dto;
import java.math.BigDecimal;
public record PeixesDashboardResumo(long lotesAtivos,long quantidadeAtual,BigDecimal biomassaEstimada,
        BigDecimal pesoMedio,BigDecimal consumoRecente,long mortalidadeRecente,long tarefasAbertas,
        long alertasAbertos,QualidadeAguaDashboardResumo qualidadeAgua) {
    public PeixesDashboardResumo(long lotesAtivos,long quantidadeAtual,BigDecimal biomassaEstimada,
            BigDecimal pesoMedio,BigDecimal consumoRecente,long mortalidadeRecente,long tarefasAbertas,long alertasAbertos){
        this(lotesAtivos,quantidadeAtual,biomassaEstimada,pesoMedio,consumoRecente,mortalidadeRecente,
                tarefasAbertas,alertasAbertos,QualidadeAguaDashboardResumo.vazio());
    }
}
