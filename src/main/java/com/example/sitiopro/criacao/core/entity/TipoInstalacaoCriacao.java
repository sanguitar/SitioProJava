package com.example.sitiopro.criacao.core.entity;

public enum TipoInstalacaoCriacao {
    INCUBADORA("Incubadora"),
    CRIADOURO_PINTINHOS("Criadouro de pintinhos"),
    GALINHEIRO("Galinheiro"),
    PIQUETE("Piquete"),
    OUTRO("Outro");

    private final String rotulo;

    TipoInstalacaoCriacao(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
