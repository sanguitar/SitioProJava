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

    @Column(name = "area_gis_m2", precision = 18, scale = 4)
    private BigDecimal areaGisM2;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private StatusDivisaoFisica status = StatusDivisaoFisica.ATIVO;

    @ElementCollection
    @CollectionTable(name = "propriedade_talhao_vertices", joinColumns = @JoinColumn(name = "talhao_id"))
    @OrderBy("ordem ASC")
    private java.util.List<VerticeTalhao> vertices = new java.util.ArrayList<>();

    public AreaPropriedade getArea() { return area; }
    public void setArea(AreaPropriedade area) { this.area = area; }
    public String getCodigo() { return codigo; }
    public BigDecimal getAreaHa() { return areaHa; }
    public void setAreaHa(BigDecimal areaHa) { this.areaHa = areaHa; }
    public BigDecimal getAreaGisM2() { return areaGisM2; }
    public StatusDivisaoFisica getStatus() { return status; }
    public void setStatus(StatusDivisaoFisica status) { this.status = status; }
    public java.util.List<VerticeTalhao> getVertices() { return java.util.List.copyOf(vertices); }
    public void substituirVertices(java.util.List<VerticeTalhao> novos) {
        vertices.clear();
        vertices.addAll(novos);
    }
}
