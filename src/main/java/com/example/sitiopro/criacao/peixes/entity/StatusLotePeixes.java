package com.example.sitiopro.criacao.peixes.entity;

public enum StatusLotePeixes {
    ATIVO("Ativo"), ENCERRADO("Encerrado");
    private final String rotulo;
    StatusLotePeixes(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
    public boolean ativo() { return this == ATIVO; }
}
