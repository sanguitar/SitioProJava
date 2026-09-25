package com.example.sitiopro.criacao.suinos.entity;

public enum MetodoReproducaoSuinos {
    COBERTURA("Cobertura"), INSEMINACAO("Inseminação");
    private final String rotulo;
    MetodoReproducaoSuinos(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
