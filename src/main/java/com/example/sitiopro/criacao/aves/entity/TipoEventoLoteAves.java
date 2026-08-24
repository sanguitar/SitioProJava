package com.example.sitiopro.criacao.aves.entity;

public enum TipoEventoLoteAves {
    ENTRADA_INICIAL("Entrada inicial"),
    ECLOSAO("Eclosão"),
    MORTALIDADE("Mortalidade"),
    DESCARTE("Descarte"),
    VENDA("Venda"),
    ABATE("Abate"),
    TRANSFERENCIA("Transferência"),
    AJUSTE_ADMINISTRATIVO("Ajuste administrativo"),
    ALIMENTACAO("Alimentação"),
    PESAGEM("Pesagem"),
    POSTURA("Postura"),
    ENCERRAMENTO("Encerramento");
    private final String rotulo;
    TipoEventoLoteAves(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
