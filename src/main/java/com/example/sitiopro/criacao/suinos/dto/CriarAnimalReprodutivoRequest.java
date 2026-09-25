package com.example.sitiopro.criacao.suinos.dto;

import com.example.sitiopro.criacao.suinos.entity.TipoAnimalReprodutivo;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CriarAnimalReprodutivoRequest {
    @NotNull private Long loteId;
    @NotNull private TipoAnimalReprodutivo tipo;
    @Size(max = 120) private String identificacao;
    private LocalDate dataNascimento;
    @DecimalMin(value = "0.0001") @Digits(integer = 15, fraction = 4) private BigDecimal pesoAtual;
    @Size(max = 1000) private String observacao;
    @NotBlank @Size(max = 100) private String chaveIdempotencia;
    public Long getLoteId() { return loteId; } public void setLoteId(Long v) { loteId = v; }
    public TipoAnimalReprodutivo getTipo() { return tipo; } public void setTipo(TipoAnimalReprodutivo v) { tipo = v; }
    public String getIdentificacao() { return identificacao; } public void setIdentificacao(String v) { identificacao = v; }
    public LocalDate getDataNascimento() { return dataNascimento; } public void setDataNascimento(LocalDate v) { dataNascimento = v; }
    public BigDecimal getPesoAtual() { return pesoAtual; } public void setPesoAtual(BigDecimal v) { pesoAtual = v; }
    public String getObservacao() { return observacao; } public void setObservacao(String v) { observacao = v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
}
