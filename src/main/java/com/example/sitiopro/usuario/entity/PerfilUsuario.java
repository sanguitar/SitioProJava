package com.example.sitiopro.usuario.entity;

public enum PerfilUsuario {
    ADMIN("Administrador"),
    OPERADOR("Operador");

    private final String rotulo;

    PerfilUsuario(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getAuthority() {
        return "ROLE_" + name();
    }
}
