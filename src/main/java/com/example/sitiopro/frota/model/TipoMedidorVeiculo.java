package com.example.sitiopro.frota.model;

public enum TipoMedidorVeiculo {
    QUILOMETRAGEM("Quilometragem", "km"),
    HORIMETRO("Horímetro", "h");

    private final String rotulo;
    private final String unidade;

    TipoMedidorVeiculo(String rotulo, String unidade) {
        this.rotulo = rotulo;
        this.unidade = unidade;
    }

    public String getRotulo() { return rotulo; }
    public String getUnidade() { return unidade; }
}
