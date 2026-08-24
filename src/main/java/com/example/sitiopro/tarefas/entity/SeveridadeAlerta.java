package com.example.sitiopro.tarefas.entity;

public enum SeveridadeAlerta {
    INFO("Informação", "severidade-info"),
    ATENCAO("Atenção", "severidade-atencao"),
    ALTA("Alta", "severidade-alta"),
    CRITICA("Crítica", "severidade-critica");

    private final String rotulo;
    private final String classeCss;

    SeveridadeAlerta(String rotulo, String classeCss) {
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
