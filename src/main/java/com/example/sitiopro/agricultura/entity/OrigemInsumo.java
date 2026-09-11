package com.example.sitiopro.agricultura.entity;

public enum OrigemInsumo {
    EXTERNA("Externa / manual"),
    ESTOQUE("Estoque do sitio");

    private final String rotulo;

    OrigemInsumo(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
