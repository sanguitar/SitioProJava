package com.example.sitiopro.administracao.dto;

public record AdministracaoResumo(
        Usuarios usuarios,
        Saude saude,
        Integracoes integracoes,
        Observabilidade observabilidade) {

    public record Usuarios(boolean disponivel, long ativos, long administradores) {
    }

    public record Saude(boolean disponivel, String aplicacao, String banco) {
    }

    public record Integracoes(boolean disponivel, long operacionais, long comFalha) {
    }

    public record Observabilidade(boolean aplicavel, String status) {
    }
}
