package com.example.sitiopro.compras.dto;

public record FornecedorResumo(
        Long id,
        String nome,
        String documento,
        String telefone,
        String email,
        boolean ativo) {
}
