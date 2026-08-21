package com.example.sitiopro.estoque.dto;

import com.example.sitiopro.estoque.entity.TipoMovimentoEstoque;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MovimentoEstoqueRequest {

    @NotNull(message = "Item é obrigatório")
    private Long itemId;

    @NotNull(message = "Tipo é obrigatório")
    private TipoMovimentoEstoque tipo;

    @NotNull(message = "Quantidade é obrigatória")
    @DecimalMin(value = "0.0001", message = "Quantidade deve ser maior que zero")
    private BigDecimal quantidade;

    private Long localOrigemId;

    private Long localDestinoId;

    @Size(max = 80, message = "Código do lote deve ter no máximo 80 caracteres")
    private String loteCodigo;

    private LocalDate validade;

    @DecimalMin(value = "0.0000", message = "Custo unitário não pode ser negativo")
    private BigDecimal custoUnitario;

    @DecimalMin(value = "0.0000", message = "Custo total não pode ser negativo")
    private BigDecimal custoTotal;

    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
    private String observacao;

    private LocalDateTime dataMovimento;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public TipoMovimentoEstoque getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimentoEstoque tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public Long getLocalOrigemId() {
        return localOrigemId;
    }

    public void setLocalOrigemId(Long localOrigemId) {
        this.localOrigemId = localOrigemId;
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

    public BigDecimal getCustoUnitario() {
        return custoUnitario;
    }

    public void setCustoUnitario(BigDecimal custoUnitario) {
        this.custoUnitario = custoUnitario;
    }

    public BigDecimal getCustoTotal() {
        return custoTotal;
    }

    public void setCustoTotal(BigDecimal custoTotal) {
        this.custoTotal = custoTotal;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDateTime getDataMovimento() {
        return dataMovimento;
    }

    public void setDataMovimento(LocalDateTime dataMovimento) {
        this.dataMovimento = dataMovimento;
    }
}
