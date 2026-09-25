package com.example.sitiopro.criacao.suinos.entity;

public enum StatusAnimalReprodutivo {
    ATIVO("Ativo"), INATIVO("Inativo");
    private final String rotulo;
    StatusAnimalReprodutivo(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
