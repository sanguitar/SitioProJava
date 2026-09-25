package com.example.sitiopro.criacao.suinos.entity;

public enum TipoEventoSuinos {
    ENTRADA_INICIAL("Entrada inicial"), ENTRADA_ANIMAIS("Entrada de animais"),
    MORTALIDADE("Mortalidade"), PERDA("Perda"), TRANSFERENCIA("Transferência"),
    PESAGEM("Pesagem"), ALIMENTACAO("Alimentação");
    private final String rotulo;
    TipoEventoSuinos(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
