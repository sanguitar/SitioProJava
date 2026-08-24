package com.example.sitiopro.tarefas.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.Duration;

@ConfigurationProperties("sitiopro.tarefas")
public class TarefasAlertasProperties {

    private boolean schedulerEnabled = true;
    private Duration alertasIntervalo = Duration.ofMinutes(5);
    private Duration recorrenciasIntervalo = Duration.ofMinutes(1);
    private Duration schedulerAtrasoInicial = Duration.ofSeconds(30);
    private int loteProximoVencimentoDias = 30;
    private BigDecimal chuva24hLimiteMm = new BigDecimal("50.0");
    private int recorrenciasLimitePorExecucao = 100;

    public boolean isSchedulerEnabled() {
        return schedulerEnabled;
    }

    public void setSchedulerEnabled(boolean schedulerEnabled) {
        this.schedulerEnabled = schedulerEnabled;
    }

    public Duration getAlertasIntervalo() {
        return alertasIntervalo;
    }

    public void setAlertasIntervalo(Duration alertasIntervalo) {
        this.alertasIntervalo = alertasIntervalo;
    }

    public Duration getRecorrenciasIntervalo() {
        return recorrenciasIntervalo;
    }

    public void setRecorrenciasIntervalo(Duration recorrenciasIntervalo) {
        this.recorrenciasIntervalo = recorrenciasIntervalo;
    }

    public Duration getSchedulerAtrasoInicial() {
        return schedulerAtrasoInicial;
    }

    public void setSchedulerAtrasoInicial(Duration schedulerAtrasoInicial) {
        this.schedulerAtrasoInicial = schedulerAtrasoInicial;
    }

    public int getLoteProximoVencimentoDias() {
        return loteProximoVencimentoDias;
    }

    public void setLoteProximoVencimentoDias(int loteProximoVencimentoDias) {
        this.loteProximoVencimentoDias = loteProximoVencimentoDias;
    }

    public BigDecimal getChuva24hLimiteMm() {
        return chuva24hLimiteMm;
    }

    public void setChuva24hLimiteMm(BigDecimal chuva24hLimiteMm) {
        this.chuva24hLimiteMm = chuva24hLimiteMm;
    }

    public int getRecorrenciasLimitePorExecucao() {
        return recorrenciasLimitePorExecucao;
    }

    public void setRecorrenciasLimitePorExecucao(int recorrenciasLimitePorExecucao) {
        this.recorrenciasLimitePorExecucao = recorrenciasLimitePorExecucao;
    }
}
