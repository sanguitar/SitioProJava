package com.example.sitiopro.criacao.aves.dto;

import com.example.sitiopro.criacao.aves.entity.TipoAcompanhamentoIncubacaoAves;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RegistrarAcompanhamentoIncubacaoAvesRequest {
    @NotNull private TipoAcompanhamentoIncubacaoAves tipo;
    @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") private LocalDateTime dataHora;
    @Min(0) private Integer quantidadeAvaliada;
    @Min(0) private Integer ovosFerteis;
    @Min(0) private Integer ovosSemDesenvolvimento;
    @Min(0) private Integer perdas;
    @Digits(integer = 3, fraction = 2) @DecimalMin("0.00") @DecimalMax("100.00") private BigDecimal temperatura;
    @Digits(integer = 3, fraction = 2) @DecimalMin("0.00") @DecimalMax("100.00") private BigDecimal umidade;
    @Size(max = 1000) private String observacao;
    @NotBlank @Size(max = 100) private String chaveIdempotencia;

    public TipoAcompanhamentoIncubacaoAves getTipo() { return tipo; }
    public void setTipo(TipoAcompanhamentoIncubacaoAves valor) { tipo = valor; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime valor) { dataHora = valor; }
    public Integer getQuantidadeAvaliada() { return quantidadeAvaliada; }
    public void setQuantidadeAvaliada(Integer valor) { quantidadeAvaliada = valor; }
    public Integer getOvosFerteis() { return ovosFerteis; }
    public void setOvosFerteis(Integer valor) { ovosFerteis = valor; }
    public Integer getOvosSemDesenvolvimento() { return ovosSemDesenvolvimento; }
    public void setOvosSemDesenvolvimento(Integer valor) { ovosSemDesenvolvimento = valor; }
    public Integer getPerdas() { return perdas; }
    public void setPerdas(Integer valor) { perdas = valor; }
    public BigDecimal getTemperatura() { return temperatura; }
    public void setTemperatura(BigDecimal valor) { temperatura = valor; }
    public BigDecimal getUmidade() { return umidade; }
    public void setUmidade(BigDecimal valor) { umidade = valor; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String valor) { observacao = valor; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String valor) { chaveIdempotencia = valor; }
}

