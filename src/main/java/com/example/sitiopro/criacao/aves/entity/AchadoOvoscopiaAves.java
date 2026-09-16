package com.example.sitiopro.criacao.aves.entity;

public enum AchadoOvoscopiaAves {
    DESENVOLVIMENTO_VISIVEL("Desenvolvimento visível"),
    SEM_DESENVOLVIMENTO_VISIVEL("Sem desenvolvimento visível"),
    RACHADURA("Rachadura"),
    DUVIDA("Dúvida"),
    REAVALIAR("Reavaliar"),
    PERDA_RETIRADA("Perda retirada");

    private final String rotulo;

    AchadoOvoscopiaAves(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}

