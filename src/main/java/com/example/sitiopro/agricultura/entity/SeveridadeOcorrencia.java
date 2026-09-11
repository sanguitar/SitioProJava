package com.example.sitiopro.agricultura.entity;

public enum SeveridadeOcorrencia {
    BAIXA("Baixa"), MEDIA("Media"), ALTA("Alta"), CRITICA("Critica");

    private final String rotulo;

    SeveridadeOcorrencia(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
