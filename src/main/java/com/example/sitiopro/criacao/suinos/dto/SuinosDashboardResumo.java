package com.example.sitiopro.criacao.suinos.dto;
import java.math.BigDecimal;
public record SuinosDashboardResumo(long lotesAtivos, long quantidadeAnimais, BigDecimal pesoMedio,
        BigDecimal consumoUltimos30Dias, long mortalidadeUltimos30Dias, long tarefasAbertas, long alertasAbertos) {}
