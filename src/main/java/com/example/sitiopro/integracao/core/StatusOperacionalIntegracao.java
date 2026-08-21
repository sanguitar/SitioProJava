package com.example.sitiopro.integracao.core;

public enum StatusOperacionalIntegracao {
    OPERACIONAL("Operacional", "status-operacional"),
    DESATUALIZADO("Desatualizado", "status-desatualizado"),
    NAO_CONFIGURADO("Não configurado", "status-nao-configurado"),
    NAO_INICIALIZADO("Não inicializado", "status-nao-inicializado"),
    DESABILITADO("Desabilitado", "status-desabilitado"),
    FALHA("Falha", "status-falha");

    private final String rotulo;
    private final String classeCss;

    StatusOperacionalIntegracao(String rotulo, String classeCss) {
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
