package com.example.sitiopro.agricultura.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class IrrigacaoRequest {
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) private LocalDateTime dataHora;
    @Min(1) @Max(10080) private Integer duracaoMinutos;
    @DecimalMin("0.0001") @Digits(integer = 14, fraction = 4) private BigDecimal volumeLitros;
    @Size(max = 120) private String metodo;
    @Size(max = 1000) private String observacao;
    @NotBlank @Size(min = 8, max = 80) @Pattern(regexp = "[A-Za-z0-9._:-]+") private String chaveIdempotencia;
    @NotNull @PositiveOrZero private Long versao;

    public LocalDateTime getDataHora() { return dataHora; } public void setDataHora(LocalDateTime v) { dataHora = v; }
    public Integer getDuracaoMinutos() { return duracaoMinutos; } public void setDuracaoMinutos(Integer v) { duracaoMinutos = v; }
    public BigDecimal getVolumeLitros() { return volumeLitros; } public void setVolumeLitros(BigDecimal v) { volumeLitros = v; }
    public String getMetodo() { return metodo; } public void setMetodo(String v) { metodo = v; }
    public String getObservacao() { return observacao; } public void setObservacao(String v) { observacao = v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
    public Long getVersao() { return versao; } public void setVersao(Long v) { versao = v; }
}
