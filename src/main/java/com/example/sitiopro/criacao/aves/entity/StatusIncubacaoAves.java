package com.example.sitiopro.criacao.aves.entity;

public enum StatusIncubacaoAves {
    EM_INCUBACAO("Em incubação", "status-development"),
    FINALIZADA("Finalizada", "status-functional"),
    CANCELADA("Cancelada", "status-review");
    private final String rotulo;
    private final String classeCss;
    StatusIncubacaoAves(String rotulo, String classeCss) { this.rotulo = rotulo; this.classeCss = classeCss; }
    public String getRotulo() { return rotulo; }
    public String getClasseCss() { return classeCss; }
}
