package com.example.sitiopro.compras.entity;

public enum TipoEmbalagem {
    UNIDADE("Unidade", "unidades"),
    PACOTE("Pacote", "pacotes"),
    SACO("Saco", "sacos"),
    CAIXA("Caixa", "caixas"),
    FARDO("Fardo", "fardos"),
    BALDE("Balde", "baldes"),
    GALAO("Galão", "galões"),
    ROLO("Rolo", "rolos"),
    OUTRO("Outro", "volumes");

    private final String rotulo;
    private final String rotuloPlural;

    TipoEmbalagem(String rotulo, String rotuloPlural) {
        this.rotulo = rotulo;
        this.rotuloPlural = rotuloPlural;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getRotuloPlural() {
        return rotuloPlural;
    }
}
