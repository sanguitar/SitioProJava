package com.example.sitiopro.manutencao.entity;

public enum TipoAtivoPatrimonial {
    MAQUINA("Máquina"), EQUIPAMENTO("Equipamento"), FERRAMENTA("Ferramenta"),
    BOMBA("Bomba"), MOTOR("Motor"), GERADOR("Gerador"), IMPLEMENTO("Implemento"),
    ELETRODOMESTICO("Eletrodoméstico"), OUTRO("Outro");

    private final String rotulo;
    TipoAtivoPatrimonial(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
