package com.example.sitiopro.agricultura.entity;

public enum StatusOcorrenciaCultivo {
    ABERTA("Aberta"),
    EM_ACOMPANHAMENTO("Em acompanhamento"),
    ENCERRADA("Encerrada");

    private final String rotulo;

    StatusOcorrenciaCultivo(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public boolean aberta() {
        return this != ENCERRADA;
    }
}
