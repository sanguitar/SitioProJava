package com.example.sitiopro.criacao.suinos.entity;

public enum StatusLoteSuinos {
    ATIVO("Ativo"), ENCERRADO("Encerrado");
    private final String rotulo;
    StatusLoteSuinos(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
    public boolean ativo() { return this == ATIVO; }
}
