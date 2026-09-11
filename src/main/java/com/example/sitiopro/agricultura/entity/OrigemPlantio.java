package com.example.sitiopro.agricultura.entity;

public enum OrigemPlantio {
    EXTERNA, ESTOQUE;
    public String getRotulo() {
        return switch (this) {
            case EXTERNA -> "Externa / manual";
            case ESTOQUE -> "Estoque";
        };
    }
}
