package com.example.sitiopro.criacao.peixes.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public class ConfiguracaoQualidadeAguaDto {
    @NotNull private BigDecimal temperaturaMin; @NotNull private BigDecimal temperaturaMax;
    @NotNull @DecimalMin("0") @DecimalMax("14") private BigDecimal phMin; @NotNull @DecimalMin("0") @DecimalMax("14") private BigDecimal phMax;
    @NotNull @DecimalMin("0") private BigDecimal oxigenioMin; @NotNull @DecimalMin("0") private BigDecimal transparenciaMinCm;
    @NotNull @DecimalMin("0") private BigDecimal amoniaMax; @NotNull @DecimalMin("0") private BigDecimal nitritoMax;
    @NotNull @Min(1) @Max(365) private Integer intervaloMedicaoDias; @PositiveOrZero private Long versao;
    public BigDecimal getTemperaturaMin(){return temperaturaMin;} public void setTemperaturaMin(BigDecimal v){temperaturaMin=v;} public BigDecimal getTemperaturaMax(){return temperaturaMax;} public void setTemperaturaMax(BigDecimal v){temperaturaMax=v;} public BigDecimal getPhMin(){return phMin;} public void setPhMin(BigDecimal v){phMin=v;} public BigDecimal getPhMax(){return phMax;} public void setPhMax(BigDecimal v){phMax=v;} public BigDecimal getOxigenioMin(){return oxigenioMin;} public void setOxigenioMin(BigDecimal v){oxigenioMin=v;} public BigDecimal getTransparenciaMinCm(){return transparenciaMinCm;} public void setTransparenciaMinCm(BigDecimal v){transparenciaMinCm=v;} public BigDecimal getAmoniaMax(){return amoniaMax;} public void setAmoniaMax(BigDecimal v){amoniaMax=v;} public BigDecimal getNitritoMax(){return nitritoMax;} public void setNitritoMax(BigDecimal v){nitritoMax=v;} public Integer getIntervaloMedicaoDias(){return intervaloMedicaoDias;} public void setIntervaloMedicaoDias(Integer v){intervaloMedicaoDias=v;} public Long getVersao(){return versao;} public void setVersao(Long v){versao=v;}
}
