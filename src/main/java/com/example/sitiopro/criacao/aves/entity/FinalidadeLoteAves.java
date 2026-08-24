package com.example.sitiopro.criacao.aves.entity;

public enum FinalidadeLoteAves {
    POSTURA("Postura"), CORTE("Corte"), REPRODUCAO("Reprodução"), MISTA("Mista");
    private final String rotulo;
    FinalidadeLoteAves(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
    public boolean permitePostura() { return this == POSTURA || this == MISTA || this == REPRODUCAO; }
}
