package com.example.sitiopro.manutencao.dto;

import com.example.sitiopro.manutencao.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AtivoPatrimonialRequest {
    @NotBlank @Size(max = 160) private String nome;
    @NotNull private TipoAtivoPatrimonial tipo;
    @Size(max = 100) private String marca;
    @Size(max = 100) private String modelo;
    @Size(max = 120) private String numeroSerie;
    @PastOrPresent private LocalDate dataAquisicao;
    @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) private BigDecimal valorAquisicao;
    @Size(max = 160) private String localizacao;
    private Long estruturaId;
    @NotNull private StatusAtivoPatrimonial status = StatusAtivoPatrimonial.ATIVO;
    @Size(max = 1000) private String observacao;
    @NotBlank @Size(max = 100) private String chaveIdempotencia;
    private Long versao;

    public String getNome() { return nome; } public void setNome(String v) { nome = v; }
    public TipoAtivoPatrimonial getTipo() { return tipo; } public void setTipo(TipoAtivoPatrimonial v) { tipo = v; }
    public String getMarca() { return marca; } public void setMarca(String v) { marca = v; }
    public String getModelo() { return modelo; } public void setModelo(String v) { modelo = v; }
    public String getNumeroSerie() { return numeroSerie; } public void setNumeroSerie(String v) { numeroSerie = v; }
    public LocalDate getDataAquisicao() { return dataAquisicao; } public void setDataAquisicao(LocalDate v) { dataAquisicao = v; }
    public BigDecimal getValorAquisicao() { return valorAquisicao; } public void setValorAquisicao(BigDecimal v) { valorAquisicao = v; }
    public String getLocalizacao() { return localizacao; } public void setLocalizacao(String v) { localizacao = v; }
    public Long getEstruturaId() { return estruturaId; } public void setEstruturaId(Long v) { estruturaId = v; }
    public StatusAtivoPatrimonial getStatus() { return status; } public void setStatus(StatusAtivoPatrimonial v) { status = v; }
    public String getObservacao() { return observacao; } public void setObservacao(String v) { observacao = v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
    public Long getVersao() { return versao; } public void setVersao(Long v) { versao = v; }
}
