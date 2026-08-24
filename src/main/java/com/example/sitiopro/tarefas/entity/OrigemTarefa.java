package com.example.sitiopro.tarefas.entity;

public enum OrigemTarefa {
    MANUAL("Manual"),
    AUTOMATICA("Automática");

    private final String rotulo;

    OrigemTarefa(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
