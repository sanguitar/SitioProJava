package com.example.sitiopro.tarefas.entity;

public enum TipoAlerta {
    ESTOQUE_ABAIXO_MINIMO("Estoque abaixo do mínimo"),
    LOTE_PROXIMO_VENCIMENTO("Lote próximo do vencimento"),
    LOTE_VENCIDO("Lote vencido"),
    INTEGRACAO_DESATUALIZADA("Integração desatualizada"),
    INTEGRACAO_COM_FALHA("Integração com falha"),
    CHUVA_INTENSA_24H("Chuva intensa nas próximas 24h");

    private final String rotulo;

    TipoAlerta(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
