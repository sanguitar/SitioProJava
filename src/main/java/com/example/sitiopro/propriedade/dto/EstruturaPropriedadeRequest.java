package com.example.sitiopro.propriedade.dto;

import com.example.sitiopro.propriedade.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class EstruturaPropriedadeRequest {
    @NotBlank @Size(max = 120)
    private String nome;

    @Size(max = 1000)
    private String observacao;

    @PositiveOrZero
    private Long versao;

    @Positive
    private Long areaId;

    @NotNull
    private TipoEstruturaPropriedade tipo;

    @Positive @Digits(integer = 15, fraction = 3)
    private BigDecimal capacidade;

    @Size(max = 30)
    private String unidadeCapacidade;

    
    private boolean ativo = true;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
    public Long getAreaId() { return areaId; }
    public void setAreaId(Long areaId) { this.areaId = areaId; }
    public TipoEstruturaPropriedade getTipo() { return tipo; }
    public void setTipo(TipoEstruturaPropriedade tipo) { this.tipo = tipo; }
    public BigDecimal getCapacidade() { return capacidade; }
    public void setCapacidade(BigDecimal capacidade) { this.capacidade = capacidade; }
    public String getUnidadeCapacidade() { return unidadeCapacidade; }
    public void setUnidadeCapacidade(String unidadeCapacidade) { this.unidadeCapacidade = unidadeCapacidade; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
