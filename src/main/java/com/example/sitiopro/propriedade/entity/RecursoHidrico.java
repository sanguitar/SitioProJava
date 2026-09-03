package com.example.sitiopro.propriedade.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "propriedade_recursos_hidricos")
public class RecursoHidrico extends CadastroFisico {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "area_id")
    private AreaPropriedade area;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private TipoRecursoHidrico tipo;

    @Column(precision = 18, scale = 3)
    private BigDecimal capacidadeLitros;

    @Column(nullable = false)
    private boolean ativo = true;

    public AreaPropriedade getArea() { return area; }
    public void setArea(AreaPropriedade area) { this.area = area; }
    public TipoRecursoHidrico getTipo() { return tipo; }
    public void setTipo(TipoRecursoHidrico tipo) { this.tipo = tipo; }
    public BigDecimal getCapacidadeLitros() { return capacidadeLitros; }
    public void setCapacidadeLitros(BigDecimal capacidadeLitros) { this.capacidadeLitros = capacidadeLitros; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
