package com.example.sitiopro.criacao.peixes.entity;

public enum TipoRegistroSanitarioPeixes {
    TRATAMENTO("Tratamento"), EXAME("Exame"), OCORRENCIA("Ocorrência"),
    MORTALIDADE_ANORMAL("Mortalidade anormal"), MANEJO("Manejo"), OUTRO("Outro");

    private final String rotulo;
    TipoRegistroSanitarioPeixes(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
