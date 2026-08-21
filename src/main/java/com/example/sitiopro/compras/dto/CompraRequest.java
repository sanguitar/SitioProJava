package com.example.sitiopro.compras.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CompraRequest {

    @NotNull(message = "Fornecedor é obrigatório")
    private Long fornecedorId;

    @NotNull(message = "Data da compra é obrigatória")
    private LocalDate dataCompra = LocalDate.now();

    @Size(max = 80, message = "Número do documento deve ter no máximo 80 caracteres")
    private String numeroDocumento;

    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
    private String observacao;

    @DecimalMin(value = "0.0000", message = "Frete não pode ser negativo")
    private BigDecimal frete = BigDecimal.ZERO;

    @DecimalMin(value = "0.0000", message = "Desconto não pode ser negativo")
    private BigDecimal desconto = BigDecimal.ZERO;

    public Long getFornecedorId() {
        return fornecedorId;
    }

    public void setFornecedorId(Long fornecedorId) {
        this.fornecedorId = fornecedorId;
    }

    public LocalDate getDataCompra() {
        return dataCompra;
    }

    public void setDataCompra(LocalDate dataCompra) {
        this.dataCompra = dataCompra;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public BigDecimal getFrete() {
        return frete;
    }

    public void setFrete(BigDecimal frete) {
        this.frete = frete;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
    }
}
