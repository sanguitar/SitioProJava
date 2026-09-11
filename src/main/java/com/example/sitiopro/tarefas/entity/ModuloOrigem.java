package com.example.sitiopro.tarefas.entity;

public enum ModuloOrigem {
    TAREFAS("Tarefas"),
    ESTOQUE("Estoque"),
    INTEGRACOES("Integrações"),
    CLIMA("Clima"),
    COMPRAS("Compras"),
    CRIACOES("Criações"),
    AGRICULTURA("Agricultura");

    private final String rotulo;

    ModuloOrigem(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
