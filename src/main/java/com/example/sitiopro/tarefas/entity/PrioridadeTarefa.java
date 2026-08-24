package com.example.sitiopro.tarefas.entity;

public enum PrioridadeTarefa {
    BAIXA("Baixa", "prioridade-baixa"),
    NORMAL("Normal", "prioridade-normal"),
    ALTA("Alta", "prioridade-alta"),
    CRITICA("Crítica", "prioridade-critica");

    private final String rotulo;
    private final String classeCss;

    PrioridadeTarefa(String rotulo, String classeCss) {
        this.rotulo = rotulo;
        this.classeCss = classeCss;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getClasseCss() {
        return classeCss;
    }
}
