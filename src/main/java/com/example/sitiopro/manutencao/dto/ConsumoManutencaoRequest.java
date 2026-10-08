package com.example.sitiopro.manutencao.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ConsumoManutencaoRequest {
    private Long itemEstoqueId;
    private Long localEstoqueId;
    @DecimalMin("0.0001") @Digits(integer = 15, fraction = 4) private BigDecimal quantidade;
    @Size(max = 100) private String loteEstoqueCodigo;

    public Long getItemEstoqueId() { return itemEstoqueId; } public void setItemEstoqueId(Long v) { itemEstoqueId = v; }
    public Long getLocalEstoqueId() { return localEstoqueId; } public void setLocalEstoqueId(Long v) { localEstoqueId = v; }
    public BigDecimal getQuantidade() { return quantidade; } public void setQuantidade(BigDecimal v) { quantidade = v; }
    public String getLoteEstoqueCodigo() { return loteEstoqueCodigo; } public void setLoteEstoqueCodigo(String v) { loteEstoqueCodigo = v; }
    public boolean vazio() { return itemEstoqueId == null && localEstoqueId == null && quantidade == null && (loteEstoqueCodigo == null || loteEstoqueCodigo.isBlank()); }
}
