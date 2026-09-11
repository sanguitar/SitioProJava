package com.example.sitiopro.agricultura.entity;

public enum StatusSafra {
    PLANEJADA, EM_ANDAMENTO, ENCERRADA, CANCELADA;

    public boolean finalizada() { return this == ENCERRADA || this == CANCELADA; }
    public String getRotulo() {
        return switch (this) {
            case PLANEJADA -> "Planejada";
            case EM_ANDAMENTO -> "Em andamento";
            case ENCERRADA -> "Encerrada";
            case CANCELADA -> "Cancelada";
        };
    }
}
