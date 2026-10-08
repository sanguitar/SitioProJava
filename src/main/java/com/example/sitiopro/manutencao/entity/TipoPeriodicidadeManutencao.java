package com.example.sitiopro.manutencao.entity;

public enum TipoPeriodicidadeManutencao {
    DIAS("Dias"),
    HORIMETRO("Horímetro"),
    QUILOMETRAGEM("Quilometragem");

    private final String rotulo;

    TipoPeriodicidadeManutencao(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
