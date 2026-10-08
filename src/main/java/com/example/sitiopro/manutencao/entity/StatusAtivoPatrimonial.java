package com.example.sitiopro.manutencao.entity;

public enum StatusAtivoPatrimonial {
    ATIVO("Ativo"), EM_MANUTENCAO("Em manutenção"), INATIVO("Inativo"), BAIXADO("Baixado");

    private final String rotulo;
    StatusAtivoPatrimonial(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
