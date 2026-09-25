package com.example.sitiopro.criacao.suinos.entity;

public enum TipoAnimalReprodutivo {
    MATRIZ("Matriz"), REPRODUTOR("Reprodutor");
    private final String rotulo;
    TipoAnimalReprodutivo(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
