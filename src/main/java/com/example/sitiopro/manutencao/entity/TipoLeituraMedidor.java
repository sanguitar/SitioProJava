package com.example.sitiopro.manutencao.entity;

public enum TipoLeituraMedidor {
    OPERACIONAL("Operacional"),
    AJUSTE_ADMINISTRATIVO("Ajuste administrativo");

    private final String rotulo;

    TipoLeituraMedidor(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
