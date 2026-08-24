package com.example.sitiopro.criacao.aves.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegistrarMortalidadeAvesRequest extends OperacaoLoteAvesRequest {
    @NotNull(message = "Quantidade é obrigatória") @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private Integer quantidade;
    @Size(max = 200) private String causa;
    public Integer getQuantidade() { return quantidade; } public void setQuantidade(Integer v) { quantidade = v; }
    public String getCausa() { return causa; } public void setCausa(String v) { causa = v; }
}
