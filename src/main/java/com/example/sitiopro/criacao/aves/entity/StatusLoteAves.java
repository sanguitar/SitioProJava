package com.example.sitiopro.criacao.aves.entity;

public enum StatusLoteAves {
    ATIVO("Ativo", "status-functional"),
    ENCERRADO("Encerrado", "status-review"),
    VENDIDO("Vendido", "status-planned"),
    ABATIDO("Abatido", "status-planned");
    private final String rotulo;
    private final String classeCss;
    StatusLoteAves(String rotulo, String classeCss) { this.rotulo = rotulo; this.classeCss = classeCss; }
    public String getRotulo() { return rotulo; }
    public String getClasseCss() { return classeCss; }
    public boolean ativo() { return this == ATIVO; }
}
