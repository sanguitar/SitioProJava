package com.example.sitiopro.criacao.aves.entity;

public enum EspecieAves {
    GALINHA("Galinha"), CODORNA("Codorna"), PATO("Pato"), PERU("Peru"), GANSO("Ganso"), OUTRA("Outra");
    private final String rotulo;
    EspecieAves(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
