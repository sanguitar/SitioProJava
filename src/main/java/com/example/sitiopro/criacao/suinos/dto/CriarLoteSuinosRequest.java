package com.example.sitiopro.criacao.suinos.dto;

import com.example.sitiopro.criacao.suinos.entity.CategoriaSuino;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CriarLoteSuinosRequest {
    @NotNull private CategoriaSuino categoria;
    @NotNull private LocalDate dataEntrada;
    private LocalDate dataNascimento;
    @NotBlank @Size(max=200) private String origem;
    @NotNull @Min(1) private Integer quantidadeInicial;
    @DecimalMin(value="0.0001") @Digits(integer=15, fraction=4) private BigDecimal pesoMedio;
    @NotNull private Long instalacaoId;
    @Size(max=1000) private String observacao;
    @NotBlank @Size(max=100) private String chaveIdempotencia;
    public CategoriaSuino getCategoria(){return categoria;} public void setCategoria(CategoriaSuino v){categoria=v;}
    public LocalDate getDataEntrada(){return dataEntrada;} public void setDataEntrada(LocalDate v){dataEntrada=v;}
    public LocalDate getDataNascimento(){return dataNascimento;} public void setDataNascimento(LocalDate v){dataNascimento=v;}
    public String getOrigem(){return origem;} public void setOrigem(String v){origem=v;}
    public Integer getQuantidadeInicial(){return quantidadeInicial;} public void setQuantidadeInicial(Integer v){quantidadeInicial=v;}
    public BigDecimal getPesoMedio(){return pesoMedio;} public void setPesoMedio(BigDecimal v){pesoMedio=v;}
    public Long getInstalacaoId(){return instalacaoId;} public void setInstalacaoId(Long v){instalacaoId=v;}
    public String getObservacao(){return observacao;} public void setObservacao(String v){observacao=v;}
    public String getChaveIdempotencia(){return chaveIdempotencia;} public void setChaveIdempotencia(String v){chaveIdempotencia=v;}
}
