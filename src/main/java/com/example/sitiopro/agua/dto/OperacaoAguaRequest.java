package com.example.sitiopro.agua.dto;

import com.example.sitiopro.agua.entity.TipoOperacaoAgua;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OperacaoAguaRequest {
    @NotNull
    private Long recursoOrigemId;

    private Long recursoDestinoId;

    @NotNull
    private TipoOperacaoAgua tipo;

    @NotNull
    private LocalDateTime dataOperacao;

    @PositiveOrZero @Digits(integer = 15, fraction = 3)
    private BigDecimal quantidadeLitros;

    @Size(max = 1000)
    private String observacao;

    @NotBlank @Size(max = 100)
    private String chaveIdempotencia;

    public Long getRecursoOrigemId() { return recursoOrigemId; }
    public void setRecursoOrigemId(Long recursoOrigemId) { this.recursoOrigemId = recursoOrigemId; }
    public Long getRecursoDestinoId() { return recursoDestinoId; }
    public void setRecursoDestinoId(Long recursoDestinoId) { this.recursoDestinoId = recursoDestinoId; }
    public TipoOperacaoAgua getTipo() { return tipo; }
    public void setTipo(TipoOperacaoAgua tipo) { this.tipo = tipo; }
    public LocalDateTime getDataOperacao() { return dataOperacao; }
    public void setDataOperacao(LocalDateTime dataOperacao) { this.dataOperacao = dataOperacao; }
    public BigDecimal getQuantidadeLitros() { return quantidadeLitros; }
    public void setQuantidadeLitros(BigDecimal quantidadeLitros) { this.quantidadeLitros = quantidadeLitros; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
}
