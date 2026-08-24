package com.example.sitiopro.tarefas.entity;

public enum TipoRecorrencia {
    NENHUMA("Não recorrente"),
    DIARIA("Diária"),
    SEMANAL("Semanal"),
    MENSAL("Mensal"),
    INTERVALO_DIAS("Intervalo em dias");

    private final String rotulo;

    TipoRecorrencia(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
