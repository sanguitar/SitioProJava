package com.example.sitiopro.criacao.peixes.entity;

public enum TipoEventoPeixes {
    ENTRADA_INICIAL("Entrada inicial"), ENTRADA_PEIXES("Entrada de peixes"),
    MORTALIDADE("Mortalidade"), PERDA("Perda"), TRANSFERENCIA("Transferência"),
    BIOMETRIA("Biometria"), ALIMENTACAO("Alimentação");
    private final String rotulo;
    TipoEventoPeixes(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
