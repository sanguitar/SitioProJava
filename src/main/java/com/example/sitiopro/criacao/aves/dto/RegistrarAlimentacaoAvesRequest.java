package com.example.sitiopro.criacao.aves.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class RegistrarAlimentacaoAvesRequest extends OperacaoLoteAvesRequest {
    @NotNull(message = "Item de estoque é obrigatório") private Long itemEstoqueId;
    @NotNull(message = "Local de estoque é obrigatório") private Long localEstoqueId;
    @NotNull(message = "Quantidade é obrigatória") @DecimalMin(value = "0.0001", message = "Quantidade deve ser maior que zero")
    private BigDecimal quantidade;
    @Size(max = 80) private String loteEstoqueCodigo;
    public Long getItemEstoqueId() { return itemEstoqueId; } public void setItemEstoqueId(Long v) { itemEstoqueId = v; }
    public Long getLocalEstoqueId() { return localEstoqueId; } public void setLocalEstoqueId(Long v) { localEstoqueId = v; }
    public BigDecimal getQuantidade() { return quantidade; } public void setQuantidade(BigDecimal v) { quantidade = v; }
    public String getLoteEstoqueCodigo() { return loteEstoqueCodigo; } public void setLoteEstoqueCodigo(String v) { loteEstoqueCodigo = v; }
}
