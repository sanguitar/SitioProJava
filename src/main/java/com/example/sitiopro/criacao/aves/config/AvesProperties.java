package com.example.sitiopro.criacao.aves.config;

import com.example.sitiopro.criacao.aves.entity.EspecieAves;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

@ConfigurationProperties("sitiopro.criacoes.aves")
public class AvesProperties {
    private BigDecimal mortalidadeAlertaPercentual = new BigDecimal("5.0");
    private int mortalidadePeriodoDias = 7;
    private int eclosaoProximaDias = 2;
    private Map<EspecieAves, Integer> periodosIncubacaoDias = periodoPadrao();
    private boolean tarefasAutomaticasEnabled = true;
    private int tarefaVerificacaoDias = 1;
    private int tarefaOvoscopiaDias = 7;
    private int tarefaPreparacaoAntecedenciaDias = 3;
    private int pintinhosRecentesDias = 30;

    public BigDecimal getMortalidadeAlertaPercentual() { return mortalidadeAlertaPercentual; }
    public void setMortalidadeAlertaPercentual(BigDecimal valor) { this.mortalidadeAlertaPercentual = valor; }
    public int getMortalidadePeriodoDias() { return mortalidadePeriodoDias; }
    public void setMortalidadePeriodoDias(int valor) { this.mortalidadePeriodoDias = valor; }
    public int getEclosaoProximaDias() { return eclosaoProximaDias; }
    public void setEclosaoProximaDias(int valor) { this.eclosaoProximaDias = valor; }
    public Map<EspecieAves, Integer> getPeriodosIncubacaoDias() { return periodosIncubacaoDias; }
    public void setPeriodosIncubacaoDias(Map<EspecieAves, Integer> valor) {
        periodosIncubacaoDias = valor == null ? new EnumMap<>(EspecieAves.class) : new EnumMap<>(valor);
    }
    public boolean isTarefasAutomaticasEnabled() { return tarefasAutomaticasEnabled; }
    public void setTarefasAutomaticasEnabled(boolean valor) { tarefasAutomaticasEnabled = valor; }
    public int getTarefaVerificacaoDias() { return tarefaVerificacaoDias; }
    public void setTarefaVerificacaoDias(int valor) { tarefaVerificacaoDias = valor; }
    public int getTarefaOvoscopiaDias() { return tarefaOvoscopiaDias; }
    public void setTarefaOvoscopiaDias(int valor) { tarefaOvoscopiaDias = valor; }
    public int getTarefaPreparacaoAntecedenciaDias() { return tarefaPreparacaoAntecedenciaDias; }
    public void setTarefaPreparacaoAntecedenciaDias(int valor) { tarefaPreparacaoAntecedenciaDias = valor; }
    public int getPintinhosRecentesDias() { return pintinhosRecentesDias; }
    public void setPintinhosRecentesDias(int valor) { pintinhosRecentesDias = valor; }

    public Integer periodoIncubacaoDias(EspecieAves especie) {
        return especie == null ? null : periodosIncubacaoDias.get(especie);
    }

    private static Map<EspecieAves, Integer> periodoPadrao() {
        Map<EspecieAves, Integer> periodos = new EnumMap<>(EspecieAves.class);
        periodos.put(EspecieAves.GALINHA, 21);
        return periodos;
    }
}
