package com.example.sitiopro.criacao.aves.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class RegistrarPesagemAvesRequest extends OperacaoLoteAvesRequest {
    @Min(value = 1, message = "A amostra deve conter ao menos uma ave") private Integer quantidadeAmostrada;
    @NotNull(message = "Peso médio é obrigatório") @DecimalMin(value = "0.0001") private BigDecimal pesoMedio;
    @DecimalMin(value = "0.0001") private BigDecimal pesoMinimo;
    @DecimalMin(value = "0.0001") private BigDecimal pesoMaximo;
    public Integer getQuantidadeAmostrada() { return quantidadeAmostrada; } public void setQuantidadeAmostrada(Integer v) { quantidadeAmostrada = v; }
    public BigDecimal getPesoMedio() { return pesoMedio; } public void setPesoMedio(BigDecimal v) { pesoMedio = v; }
    public BigDecimal getPesoMinimo() { return pesoMinimo; } public void setPesoMinimo(BigDecimal v) { pesoMinimo = v; }
    public BigDecimal getPesoMaximo() { return pesoMaximo; } public void setPesoMaximo(BigDecimal v) { pesoMaximo = v; }
}
