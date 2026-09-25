package com.example.sitiopro.criacao.suinos.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class RegistrarPartoSuinosRequest {
    @NotNull private LocalDate dataParto;
    @NotNull @Min(0) private Integer nascidosVivos;
    @NotNull @Min(0) private Integer natimortos;
    @NotNull @Min(0) private Integer perdas;
    private Long instalacaoLeitoesId;
    @DecimalMin(value = "0.0001") @Digits(integer = 15, fraction = 4) private BigDecimal pesoMedioLeitoes;
    @Size(max = 1000) private String observacao;
    @NotBlank @Size(max = 100) private String chaveIdempotencia;
    public LocalDate getDataParto() { return dataParto; } public void setDataParto(LocalDate v) { dataParto = v; }
    public Integer getNascidosVivos() { return nascidosVivos; } public void setNascidosVivos(Integer v) { nascidosVivos = v; }
    public Integer getNatimortos() { return natimortos; } public void setNatimortos(Integer v) { natimortos = v; }
    public Integer getPerdas() { return perdas; } public void setPerdas(Integer v) { perdas = v; }
    public Long getInstalacaoLeitoesId() { return instalacaoLeitoesId; } public void setInstalacaoLeitoesId(Long v) { instalacaoLeitoesId = v; }
    public BigDecimal getPesoMedioLeitoes() { return pesoMedioLeitoes; } public void setPesoMedioLeitoes(BigDecimal v) { pesoMedioLeitoes = v; }
    public String getObservacao() { return observacao; } public void setObservacao(String v) { observacao = v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
}
