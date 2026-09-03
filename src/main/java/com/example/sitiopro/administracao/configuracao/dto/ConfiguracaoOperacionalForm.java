package com.example.sitiopro.administracao.configuracao.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ConfiguracaoOperacionalForm {
    @jakarta.validation.constraints.PositiveOrZero
    private Long propriedadeVersao;
    public Long getPropriedadeVersao() { return propriedadeVersao; }
    public void setPropriedadeVersao(Long propriedadeVersao) { this.propriedadeVersao = propriedadeVersao; }

    @NotBlank(message = "Informe o nome da propriedade.")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
    private String nomePropriedade;

    @NotBlank(message = "Informe o timezone.")
    @Size(max = 80, message = "O timezone deve ter no máximo 80 caracteres.")
    private String timezone;

    @DecimalMin(value = "-90", message = "A latitude mínima é -90.")
    @DecimalMax(value = "90", message = "A latitude máxima é 90.")
    @Digits(integer = 2, fraction = 6, message = "Use até 6 casas decimais na latitude.")
    private BigDecimal latitude;

    @DecimalMin(value = "-180", message = "A longitude mínima é -180.")
    @DecimalMax(value = "180", message = "A longitude máxima é 180.")
    @Digits(integer = 3, fraction = 6, message = "Use até 6 casas decimais na longitude.")
    private BigDecimal longitude;

    @NotNull(message = "Informe os dias padrão de incubação.")
    @Min(value = 1, message = "O período de incubação deve ter ao menos 1 dia.")
    @Max(value = 120, message = "O período de incubação deve ter no máximo 120 dias.")
    private Integer diasPadraoIncubacao;

    @NotNull(message = "Informe a antecedência do alerta.")
    @Min(value = 0, message = "A antecedência não pode ser negativa.")
    @Max(value = 30, message = "A antecedência deve ter no máximo 30 dias.")
    private Integer antecedenciaAlertaEclosaoDias;

    public String getNomePropriedade() { return nomePropriedade; }
    public void setNomePropriedade(String valor) { nomePropriedade = valor; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String valor) { timezone = valor; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal valor) { latitude = valor; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal valor) { longitude = valor; }
    public Integer getDiasPadraoIncubacao() { return diasPadraoIncubacao; }
    public void setDiasPadraoIncubacao(Integer valor) { diasPadraoIncubacao = valor; }
    public Integer getAntecedenciaAlertaEclosaoDias() { return antecedenciaAlertaEclosaoDias; }
    public void setAntecedenciaAlertaEclosaoDias(Integer valor) { antecedenciaAlertaEclosaoDias = valor; }
}
