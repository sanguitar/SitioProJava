package com.example.sitiopro.criacao.suinos.entity;

public enum TipoRegistroSanitarioSuinos {
    VACINACAO("Vacinação"),
    VERMIFUGACAO("Vermifugação"),
    TRATAMENTO("Tratamento"),
    EXAME("Exame"),
    OCORRENCIA("Ocorrência"),
    OUTRO("Outro");

    private final String rotulo;
    TipoRegistroSanitarioSuinos(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
