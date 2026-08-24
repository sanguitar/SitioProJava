package com.example.sitiopro.tarefas.entity;

public enum StatusAlerta {
    ATIVO("Ativo", "status-alerta-ativo"),
    RECONHECIDO("Reconhecido", "status-alerta-reconhecido"),
    RESOLVIDO("Resolvido", "status-alerta-resolvido");

    private final String rotulo;
    private final String classeCss;

    StatusAlerta(String rotulo, String classeCss) {
        this.rotulo = rotulo;
        this.classeCss = classeCss;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getClasseCss() {
        return classeCss;
    }
}
