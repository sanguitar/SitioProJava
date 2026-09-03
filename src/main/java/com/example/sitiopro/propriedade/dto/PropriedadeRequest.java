package com.example.sitiopro.propriedade.dto;

import com.example.sitiopro.propriedade.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class PropriedadeRequest {
    @NotBlank @Size(max = 120)
    private String nome;

    @Size(max = 1000)
    private String observacao;

    @PositiveOrZero
    private Long versao;

    @Size(max = 120)
    private String municipio;

    @Pattern(regexp = "AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO", message = "Informe uma UF brasileira válida.")
    private String uf;

    @Positive @Digits(integer = 10, fraction = 4)
    private BigDecimal areaTotalHa;

    @DecimalMin("-90") @DecimalMax("90") @Digits(integer = 3, fraction = 6)
    private BigDecimal latitudeCentral;

    @DecimalMin("-180") @DecimalMax("180") @Digits(integer = 3, fraction = 6)
    private BigDecimal longitudeCentral;

    
    private boolean ativo = true;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }
    public String getUf() { return uf; }
    public void setUf(String uf) {
        this.uf = uf == null || uf.isBlank() ? null : uf.trim().toUpperCase(java.util.Locale.ROOT);
    }
    public BigDecimal getAreaTotalHa() { return areaTotalHa; }
    public void setAreaTotalHa(BigDecimal areaTotalHa) { this.areaTotalHa = areaTotalHa; }
    public BigDecimal getLatitudeCentral() { return latitudeCentral; }
    public void setLatitudeCentral(BigDecimal latitudeCentral) { this.latitudeCentral = latitudeCentral; }
    public BigDecimal getLongitudeCentral() { return longitudeCentral; }
    public void setLongitudeCentral(BigDecimal longitudeCentral) { this.longitudeCentral = longitudeCentral; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
