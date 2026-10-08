package com.example.sitiopro.manutencao.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LeituraMedidorRequest {
    @NotNull private Long ativoId;
    @NotNull @PastOrPresent private LocalDateTime dataLeitura;
    @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) private BigDecimal horimetro;
    @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) private BigDecimal quilometragem;
    @Size(max = 500) private String justificativaAjuste;
    @NotBlank @Size(max = 100) private String chaveIdempotencia;

    public Long getAtivoId() { return ativoId; } public void setAtivoId(Long v) { ativoId = v; }
    public LocalDateTime getDataLeitura() { return dataLeitura; } public void setDataLeitura(LocalDateTime v) { dataLeitura = v; }
    public BigDecimal getHorimetro() { return horimetro; } public void setHorimetro(BigDecimal v) { horimetro = v; }
    public BigDecimal getQuilometragem() { return quilometragem; } public void setQuilometragem(BigDecimal v) { quilometragem = v; }
    public String getJustificativaAjuste() { return justificativaAjuste; } public void setJustificativaAjuste(String v) { justificativaAjuste = v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
}
