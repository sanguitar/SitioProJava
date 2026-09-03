package com.example.sitiopro.propriedade.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "propriedade_areas")
public class AreaPropriedade extends CadastroFisico {
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private TipoAreaPropriedade tipo;

    @Column(precision = 14, scale = 4)
    private BigDecimal areaHa;

    @Column(nullable = false)
    private boolean ativo = true;

    public TipoAreaPropriedade getTipo() { return tipo; }
    public void setTipo(TipoAreaPropriedade tipo) { this.tipo = tipo; }
    public BigDecimal getAreaHa() { return areaHa; }
    public void setAreaHa(BigDecimal areaHa) { this.areaHa = areaHa; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
