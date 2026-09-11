package com.example.sitiopro.agricultura.entity;

public enum TipoAcompanhamentoCultivo {
    GERAL, GERMINACAO, DESENVOLVIMENTO, FLORACAO, FRUTIFICACAO, PRAGA, DOENCA, DEFICIENCIA, PERDA, OUTRO;
    public String getRotulo() {
        return switch (this) {
            case GERAL -> "Geral";
            case GERMINACAO -> "Germinacao";
            case DESENVOLVIMENTO -> "Desenvolvimento";
            case FLORACAO -> "Floracao";
            case FRUTIFICACAO -> "Frutificacao";
            case PRAGA -> "Praga";
            case DOENCA -> "Doenca";
            case DEFICIENCIA -> "Deficiencia";
            case PERDA -> "Perda";
            case OUTRO -> "Outro";
        };
    }
}
