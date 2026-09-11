package com.example.sitiopro.agricultura.entity;

public enum TipoOcorrenciaCultivo {
    PRAGA("Praga"),
    DOENCA("Doenca"),
    DEFICIENCIA("Deficiencia"),
    DANO_CLIMATICO("Dano climatico"),
    PLANTA_DANINHA("Planta daninha"),
    OUTRO("Outro");

    private final String rotulo;

    TipoOcorrenciaCultivo(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
