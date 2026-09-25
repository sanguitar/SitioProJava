package com.example.sitiopro.criacao.suinos.entity;

public enum CategoriaSuino {
    LEITAO("Leitão"), CRESCIMENTO("Crescimento"), TERMINACAO("Terminação"),
    MATRIZ("Matriz"), REPRODUTOR("Reprodutor");
    private final String rotulo;
    CategoriaSuino(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
