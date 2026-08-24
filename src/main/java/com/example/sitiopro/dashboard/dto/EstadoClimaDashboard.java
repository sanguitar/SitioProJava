package com.example.sitiopro.dashboard.dto;

public enum EstadoClimaDashboard {
    NORMAL("Atualizado", "nivel-normal"),
    DESATUALIZADO("Desatualizado", "nivel-atencao"),
    SEM_DADOS("Sem dados", "nivel-atencao");

    private final String rotulo;
    private final String classeCss;

    EstadoClimaDashboard(String rotulo, String classeCss) {
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
