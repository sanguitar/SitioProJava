package com.example.sitiopro.tarefas.dto;

public enum PrazoTarefa {
    TODAS("Todos os prazos"),
    VENCIDAS("Vencidas"),
    HOJE("Hoje"),
    PROXIMAS("Próximas");

    private final String rotulo;

    PrazoTarefa(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
