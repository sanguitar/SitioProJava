package com.example.sitiopro.propriedade.dto;

import com.example.sitiopro.propriedade.entity.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TalhaoRequest {
    @NotBlank @Size(max = 120)
    private String nome;

    @Size(max = 1000)
    private String observacao;

    @PositiveOrZero
    private Long versao;

    @Positive
    private Long areaId;

    @NotNull @Positive @Digits(integer = 10, fraction = 4)
    private BigDecimal areaHa;

    @NotNull
    private StatusDivisaoFisica status = StatusDivisaoFisica.ATIVO;

    @Valid
    private List<@NotNull VerticeTalhaoRequest> vertices = new ArrayList<>();

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
    public Long getAreaId() { return areaId; }
    public void setAreaId(Long areaId) { this.areaId = areaId; }
    public BigDecimal getAreaHa() { return areaHa; }
    public void setAreaHa(BigDecimal areaHa) { this.areaHa = areaHa; }
    public StatusDivisaoFisica getStatus() { return status; }
    public void setStatus(StatusDivisaoFisica status) { this.status = status; }
    public List<VerticeTalhaoRequest> getVertices() { return vertices; }
    public void setVertices(List<VerticeTalhaoRequest> vertices) {
        this.vertices = vertices == null ? new ArrayList<>() : vertices;
    }
}
