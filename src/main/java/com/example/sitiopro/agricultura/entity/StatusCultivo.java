package com.example.sitiopro.agricultura.entity;

public enum StatusCultivo {
    PLANEJADO, IMPLANTADO, EM_DESENVOLVIMENTO, PRONTO_COLHEITA, COLHIDO, PERDIDO, CANCELADO;

    public boolean finalizado() { return this == COLHIDO || this == PERDIDO || this == CANCELADO; }
    public String getRotulo() {
        return switch (this) {
            case PLANEJADO -> "Planejado";
            case IMPLANTADO -> "Implantado";
            case EM_DESENVOLVIMENTO -> "Em desenvolvimento";
            case PRONTO_COLHEITA -> "Pronto para colheita";
            case COLHIDO -> "Colhido";
            case PERDIDO -> "Perdido";
            case CANCELADO -> "Cancelado";
        };
    }
}
