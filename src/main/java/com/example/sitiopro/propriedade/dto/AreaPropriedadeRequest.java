package com.example.sitiopro.propriedade.dto;

import com.example.sitiopro.propriedade.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class AreaPropriedadeRequest {
    @NotBlank @Size(max = 120)
    private String nome;

    @Size(max = 1000)
    private String observacao;

    @PositiveOrZero
    private Long versao;

    @NotNull
    private TipoAreaPropriedade tipo;

    @Positive @Digits(integer = 10, fraction = 4)
    private BigDecimal areaHa;

    
    private boolean ativo = true;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
    public TipoAreaPropriedade getTipo() { return tipo; }
    public void setTipo(TipoAreaPropriedade tipo) { this.tipo = tipo; }
    public BigDecimal getAreaHa() { return areaHa; }
    public void setAreaHa(BigDecimal areaHa) { this.areaHa = areaHa; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
