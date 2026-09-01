package com.example.sitiopro.criacao.aves.entity;

public enum TipoAcompanhamentoIncubacaoAves {
    VERIFICACAO_GERAL("Verificação geral"),
    OVOSCOPIA("Ovoscopia"),
    PERDA_RETIRADA("Perda retirada"),
    TEMPERATURA_UMIDADE("Temperatura e umidade"),
    OUTRO("Outro");

    private final String rotulo;

    TipoAcompanhamentoIncubacaoAves(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}

