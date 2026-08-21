package com.example.sitiopro.compras.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ItemCompraRequest {

    @NotNull(message = "Item de estoque é obrigatório")
    private Long itemEstoqueId;

    @NotNull(message = "Quantidade é obrigatória")
    @DecimalMin(value = "0.0001", message = "Quantidade deve ser maior que zero")
    private BigDecimal quantidade;

    @NotNull(message = "Custo unitário é obrigatório")
    @DecimalMin(value = "0.0000", message = "Custo unitário não pode ser negativo")
    private BigDecimal custoUnitario;

    @NotNull(message = "Local de destino é obrigatório")
    private Long localDestinoId;

    @Size(max = 80, message = "Código do lote deve ter no máximo 80 caracteres")
    private String loteCodigo;

    private LocalDate validade;

    public Long getItemEstoqueId() {
        return itemEstoqueId;
    }

    public void setItemEstoqueId(Long itemEstoqueId) {
        this.itemEstoqueId = itemEstoqueId;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getCustoUnitario() {
        return custoUnitario;
    }

    public void setCustoUnitario(BigDecimal custoUnitario) {
        this.custoUnitario = custoUnitario;
    }

    public Long getLocalDestinoId() {
        return localDestinoId;
    }

    public void setLocalDestinoId(Long localDestinoId) {
        this.localDestinoId = localDestinoId;
    }

    public String getLoteCodigo() {
        return loteCodigo;
    }

    public void setLoteCodigo(String loteCodigo) {
        this.loteCodigo = loteCodigo;
    }

    public LocalDate getValidade() {
        return validade;
    }

    public void setValidade(LocalDate validade) {
        this.validade = validade;
    }
}
