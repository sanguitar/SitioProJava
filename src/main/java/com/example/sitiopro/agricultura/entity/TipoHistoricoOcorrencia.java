package com.example.sitiopro.agricultura.entity;

public enum TipoHistoricoOcorrencia {
    REGISTRO("Registro"),
    ATUALIZACAO("Atualizacao"),
    ENCERRAMENTO("Encerramento");

    private final String rotulo;

    TipoHistoricoOcorrencia(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
