package com.example.sitiopro.dashboard.dto;

public enum NivelAtencao {
    NORMAL("Normal", "nivel-normal"),
    ATENCAO("Atenção", "nivel-atencao"),
    ALTA("Alta", "nivel-alta"),
    CRITICA("Crítica", "nivel-critica");

    private final String rotulo;
    private final String classeCss;

    NivelAtencao(String rotulo, String classeCss) {
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
