package com.example.sitiopro.estoque.entity;

public enum TipoMovimentoEstoque {
    ENTRADA("Entrada"),
    CONSUMO("Consumo"),
    PERDA("Perda"),
    AJUSTE_ENTRADA("Ajuste de entrada"),
    AJUSTE_SAIDA("Ajuste de saída"),
    TRANSFERENCIA("Transferência"),
    DESCARTE("Descarte");

    private final String rotulo;

    TipoMovimentoEstoque(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public boolean aumentaDestino() {
        return this == ENTRADA || this == AJUSTE_ENTRADA || this == TRANSFERENCIA;
    }

    public boolean reduzOrigem() {
        return this == CONSUMO
                || this == PERDA
                || this == AJUSTE_SAIDA
                || this == DESCARTE
                || this == TRANSFERENCIA;
    }

    public boolean ajusteAdministrativo() {
        return this == AJUSTE_ENTRADA || this == AJUSTE_SAIDA;
    }

    public boolean entradaComCusto() {
        return this == ENTRADA;
    }
}
