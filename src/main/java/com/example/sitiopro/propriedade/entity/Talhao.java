package com.example.sitiopro.propriedade.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "propriedade_talhoes")
public class Talhao extends CadastroFisico {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "area_id")
    private AreaPropriedade area;

    @Column(insertable = false, updatable = false)
    private String codigo;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal areaHa;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private StatusDivisaoFisica status = StatusDivisaoFisica.ATIVO;

    public AreaPropriedade getArea() { return area; }
    public void setArea(AreaPropriedade area) { this.area = area; }
    public String getCodigo() { return codigo; }
    public BigDecimal getAreaHa() { return areaHa; }
    public void setAreaHa(BigDecimal areaHa) { this.areaHa = areaHa; }
    public StatusDivisaoFisica getStatus() { return status; }
    public void setStatus(StatusDivisaoFisica status) { this.status = status; }
}
