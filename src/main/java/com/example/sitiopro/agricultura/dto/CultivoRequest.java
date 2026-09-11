package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.agricultura.entity.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public class CultivoRequest {
    @NotNull @Positive
    private Long safraId;
    @NotNull @Positive
    private Long talhaoId;
    @NotNull @Positive
    private Long culturaId;
    @NotNull @DecimalMin("0.0001") @Digits(integer = 10, fraction = 4)
    private BigDecimal areaCultivadaHa;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataPlantio;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate previsaoColheita;
    @Size(max = 1000)
    private String observacao;
    @PositiveOrZero
    private Long versao;

    public Long getSafraId() { return safraId; }
    public void setSafraId(Long safraId) { this.safraId = safraId; }
    public Long getTalhaoId() { return talhaoId; }
    public void setTalhaoId(Long talhaoId) { this.talhaoId = talhaoId; }
    public Long getCulturaId() { return culturaId; }
    public void setCulturaId(Long culturaId) { this.culturaId = culturaId; }
    public BigDecimal getAreaCultivadaHa() { return areaCultivadaHa; }
    public void setAreaCultivadaHa(BigDecimal areaCultivadaHa) { this.areaCultivadaHa = areaCultivadaHa; }
    public LocalDate getDataPlantio() { return dataPlantio; }
    public void setDataPlantio(LocalDate dataPlantio) { this.dataPlantio = dataPlantio; }
    public LocalDate getPrevisaoColheita() { return previsaoColheita; }
    public void setPrevisaoColheita(LocalDate previsaoColheita) { this.previsaoColheita = previsaoColheita; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
}
