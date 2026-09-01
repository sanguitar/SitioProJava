package com.example.sitiopro.criacao.aves.entity;

public enum MetodoIncubacaoAves {
    CHOCADEIRA("Chocadeira"),
    GALINHA_CHOCA("Galinha choca");

    private final String rotulo;

    MetodoIncubacaoAves(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}

