package com.example.sitiopro.criacao.suinos.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class RegistrarDesmameSuinosRequest {
    @NotNull private LocalDate dataDesmame;
    private Long instalacaoDestinoId;
    @DecimalMin(value = "0.0001") @Digits(integer = 15, fraction = 4) private BigDecimal pesoMedio;
    @Size(max = 1000) private String observacao;
    @NotBlank @Size(max = 100) private String chaveIdempotencia;
    public LocalDate getDataDesmame() { return dataDesmame; } public void setDataDesmame(LocalDate v) { dataDesmame = v; }
    public Long getInstalacaoDestinoId() { return instalacaoDestinoId; } public void setInstalacaoDestinoId(Long v) { instalacaoDestinoId = v; }
    public BigDecimal getPesoMedio() { return pesoMedio; } public void setPesoMedio(BigDecimal v) { pesoMedio = v; }
    public String getObservacao() { return observacao; } public void setObservacao(String v) { observacao = v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
}
