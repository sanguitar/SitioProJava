package com.example.sitiopro.agua.entity;

public enum TipoOperacaoAgua {
    MEDICAO_NIVEL("Medição de nível"),
    ABASTECIMENTO("Abastecimento"),
    CONSUMO("Consumo"),
    RETIRADA("Retirada"),
    TRANSFERENCIA("Transferência"),
    INSPECAO("Inspeção");

    private final String rotulo;

    TipoOperacaoAgua(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
