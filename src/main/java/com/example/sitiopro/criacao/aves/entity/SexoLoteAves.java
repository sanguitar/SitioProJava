package com.example.sitiopro.criacao.aves.entity;

public enum SexoLoteAves {
    FEMEAS("Fêmeas"), MACHOS("Machos"), MISTO("Misto"), NAO_DEFINIDO("Não definido");
    private final String rotulo;
    SexoLoteAves(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
