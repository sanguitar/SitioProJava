package com.example.sitiopro.compras.entity;

public enum StatusCompra {
    RASCUNHO("Rascunho"),
    CONFIRMADA("Confirmada"),
    CANCELADA("Cancelada");

    private final String rotulo;

    StatusCompra(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public boolean isRascunho() {
        return this == RASCUNHO;
    }
}
