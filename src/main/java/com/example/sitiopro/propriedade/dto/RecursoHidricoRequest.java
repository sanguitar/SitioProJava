package com.example.sitiopro.propriedade.dto;

import com.example.sitiopro.propriedade.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class RecursoHidricoRequest {
    @NotBlank @Size(max = 120)
    private String nome;

    @Size(max = 1000)
    private String observacao;

    @PositiveOrZero
    private Long versao;

    @Positive
    private Long areaId;

    @NotNull
    private TipoRecursoHidrico tipo;

    @Positive @Digits(integer = 15, fraction = 3)
    private BigDecimal capacidadeLitros;

    
    private boolean ativo = true;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
    public Long getAreaId() { return areaId; }
    public void setAreaId(Long areaId) { this.areaId = areaId; }
    public TipoRecursoHidrico getTipo() { return tipo; }
    public void setTipo(TipoRecursoHidrico tipo) { this.tipo = tipo; }
    public BigDecimal getCapacidadeLitros() { return capacidadeLitros; }
    public void setCapacidadeLitros(BigDecimal capacidadeLitros) { this.capacidadeLitros = capacidadeLitros; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
