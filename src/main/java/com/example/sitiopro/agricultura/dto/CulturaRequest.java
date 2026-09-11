package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.agricultura.entity.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public class CulturaRequest {
    @NotBlank @Size(max = 180)
    private String nomeComum;
    @Size(max = 180)
    private String nomeCientifico;
    @Min(1) @Max(3650)
    private Integer cicloDiasEstimado;
    @Positive
    private Long agrofitCulturaId;
    private boolean ativo = true;
    @Size(max = 1000)
    private String observacao;
    @PositiveOrZero
    private Long versao;

    public String getNomeComum() { return nomeComum; }
    public void setNomeComum(String nomeComum) { this.nomeComum = nomeComum; }
    public String getNomeCientifico() { return nomeCientifico; }
    public void setNomeCientifico(String nomeCientifico) { this.nomeCientifico = nomeCientifico; }
    public Integer getCicloDiasEstimado() { return cicloDiasEstimado; }
    public void setCicloDiasEstimado(Integer cicloDiasEstimado) { this.cicloDiasEstimado = cicloDiasEstimado; }
    public Long getAgrofitCulturaId() { return agrofitCulturaId; }
    public void setAgrofitCulturaId(Long agrofitCulturaId) { this.agrofitCulturaId = agrofitCulturaId; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
}
