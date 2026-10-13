package com.example.sitiopro.agua.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RecursoHidricoOperacaoConfigRequest {
    @PositiveOrZero @Digits(integer = 15, fraction = 3)
    private BigDecimal nivelMinimoAlertaLitros;

    @Min(1) @Max(365)
    private int intervaloInspecaoDias = 7;

    @NotNull
    private LocalDateTime proximaInspecao;

    @NotNull @PositiveOrZero
    private Long versao;

    public BigDecimal getNivelMinimoAlertaLitros() { return nivelMinimoAlertaLitros; }
    public void setNivelMinimoAlertaLitros(BigDecimal valor) { this.nivelMinimoAlertaLitros = valor; }
    public int getIntervaloInspecaoDias() { return intervaloInspecaoDias; }
    public void setIntervaloInspecaoDias(int valor) { this.intervaloInspecaoDias = valor; }
    public LocalDateTime getProximaInspecao() { return proximaInspecao; }
    public void setProximaInspecao(LocalDateTime valor) { this.proximaInspecao = valor; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
}
