package com.example.sitiopro.criacao.aves.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties("sitiopro.criacoes.aves")
public class AvesProperties {
    private BigDecimal mortalidadeAlertaPercentual = new BigDecimal("5.0");
    private int mortalidadePeriodoDias = 7;
    private int eclosaoProximaDias = 2;

    public BigDecimal getMortalidadeAlertaPercentual() { return mortalidadeAlertaPercentual; }
    public void setMortalidadeAlertaPercentual(BigDecimal valor) { this.mortalidadeAlertaPercentual = valor; }
    public int getMortalidadePeriodoDias() { return mortalidadePeriodoDias; }
    public void setMortalidadePeriodoDias(int valor) { this.mortalidadePeriodoDias = valor; }
    public int getEclosaoProximaDias() { return eclosaoProximaDias; }
    public void setEclosaoProximaDias(int valor) { this.eclosaoProximaDias = valor; }
}
