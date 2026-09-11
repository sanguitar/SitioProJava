package com.example.sitiopro.agricultura.entity;

public enum DestinoColheita {
    SEM_ESTOQUE("Sem entrada no Estoque"),
    ESTOQUE("Entrada no Estoque");

    private final String rotulo;

    DestinoColheita(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
