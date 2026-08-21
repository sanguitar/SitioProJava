package com.example.sitiopro.integracao.core;

public record ResultadoSincronizacao(
        int lidos,
        int inseridos,
        int atualizados,
        int ignorados) {

    public ResultadoSincronizacao {
        if (lidos < 0 || inseridos < 0 || atualizados < 0 || ignorados < 0) {
            throw new IllegalArgumentException("Contadores de sincronização não podem ser negativos.");
        }
    }
}
