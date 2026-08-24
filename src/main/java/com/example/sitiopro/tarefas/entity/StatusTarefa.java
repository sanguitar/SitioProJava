package com.example.sitiopro.tarefas.entity;

public enum StatusTarefa {
    PENDENTE("Pendente", "status-pendente"),
    EM_ANDAMENTO("Em andamento", "status-em-andamento"),
    CONCLUIDA("Concluída", "status-concluida"),
    CANCELADA("Cancelada", "status-cancelada");

    private final String rotulo;
    private final String classeCss;

    StatusTarefa(String rotulo, String classeCss) {
        this.rotulo = rotulo;
        this.classeCss = classeCss;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getClasseCss() {
        return classeCss;
    }

    public boolean finalizado() {
        return this == CONCLUIDA || this == CANCELADA;
    }
}
