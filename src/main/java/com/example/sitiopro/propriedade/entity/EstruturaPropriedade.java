package com.example.sitiopro.propriedade.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "propriedade_estruturas")
public class EstruturaPropriedade extends CadastroFisico {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "area_id")
    private AreaPropriedade area;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private TipoEstruturaPropriedade tipo;

    @Column(precision = 18, scale = 3)
    private BigDecimal capacidade;

    @Column(length = 30)
    private String unidadeCapacidade;

    @Column(nullable = false)
    private boolean ativo = true;

    public AreaPropriedade getArea() { return area; }
    public void setArea(AreaPropriedade area) { this.area = area; }
    public TipoEstruturaPropriedade getTipo() { return tipo; }
    public void setTipo(TipoEstruturaPropriedade tipo) { this.tipo = tipo; }
    public BigDecimal getCapacidade() { return capacidade; }
    public void setCapacidade(BigDecimal capacidade) { this.capacidade = capacidade; }
    public String getUnidadeCapacidade() { return unidadeCapacidade; }
    public void setUnidadeCapacidade(String unidadeCapacidade) { this.unidadeCapacidade = unidadeCapacidade; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
