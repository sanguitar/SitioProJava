package com.example.sitiopro.criacao.peixes.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public class CriarLotePeixesRequest {
    @NotBlank @Size(max=120) private String especie;
    @NotNull @Positive private Integer quantidadeInicial;
    @NotNull private LocalDate dataEntrada;
    @NotBlank @Size(max=200) private String origem;
    @DecimalMin(value="0.0001") private BigDecimal pesoMedio;
    @NotNull private Long instalacaoId;
    @Size(max=1000) private String observacao;
    @NotBlank @Size(max=100) private String chaveIdempotencia;
    public String getEspecie(){return especie;} public void setEspecie(String v){especie=v;}
    public Integer getQuantidadeInicial(){return quantidadeInicial;} public void setQuantidadeInicial(Integer v){quantidadeInicial=v;}
    public LocalDate getDataEntrada(){return dataEntrada;} public void setDataEntrada(LocalDate v){dataEntrada=v;}
    public String getOrigem(){return origem;} public void setOrigem(String v){origem=v;}
    public BigDecimal getPesoMedio(){return pesoMedio;} public void setPesoMedio(BigDecimal v){pesoMedio=v;}
    public Long getInstalacaoId(){return instalacaoId;} public void setInstalacaoId(Long v){instalacaoId=v;}
    public String getObservacao(){return observacao;} public void setObservacao(String v){observacao=v;}
    public String getChaveIdempotencia(){return chaveIdempotencia;} public void setChaveIdempotencia(String v){chaveIdempotencia=v;}
}
