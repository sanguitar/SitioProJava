package com.example.sitiopro.propriedade.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "propriedade_perimetros")
public class PerimetroPropriedade extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propriedade_id", nullable = false, unique = true)
    private Propriedade propriedade;
    @Enumerated(EnumType.STRING) @Column(name = "status_crs", nullable = false, length = 20)
    private StatusCrs statusCrs = StatusCrs.NAO_CONFIRMADO;
    @Column(length = 120) private String crs;
    @Column(length = 120) private String datum;
    @Column(name = "sistema_geodesico", length = 80) private String sistemaGeodesico;
    @Column(name = "crs_epsg") private Integer crsEpsg;
    @Column(name = "area_documental_ha", precision = 14, scale = 4) private BigDecimal areaDocumentalHa;
    @Column(name = "perimetro_documental_m", precision = 14, scale = 2) private BigDecimal perimetroDocumentalM;
    @Column(name = "area_calculada_m2", precision = 18, scale = 4) private BigDecimal areaCalculadaM2;
    @Column(name = "perimetro_calculado_m", precision = 18, scale = 4) private BigDecimal perimetroCalculadoM;
    @Column(length = 1000) private String observacao;
    @Version @Column(nullable = false) private long versao;
    @ElementCollection
    @CollectionTable(name = "propriedade_perimetro_vertices", joinColumns = @JoinColumn(name = "perimetro_id"))
    @OrderBy("ordem ASC")
    private List<VerticePerimetro> vertices = new ArrayList<>();

    public Long getId() { return id; }
    public Propriedade getPropriedade() { return propriedade; }
    public void setPropriedade(Propriedade propriedade) { this.propriedade = propriedade; }
    public StatusCrs getStatusCrs() { return statusCrs; }
    public String getCrs() { return crs; }
    public String getDatum() { return datum; }
    public String getSistemaGeodesico() { return sistemaGeodesico; }
    public Integer getCrsEpsg() { return crsEpsg; }
    public BigDecimal getAreaDocumentalHa() { return areaDocumentalHa; }
    public BigDecimal getPerimetroDocumentalM() { return perimetroDocumentalM; }
    public BigDecimal getAreaCalculadaM2() { return areaCalculadaM2; }
    public BigDecimal getPerimetroCalculadoM() { return perimetroCalculadoM; }
    public String getObservacao() { return observacao; }
    public long getVersao() { return versao; }
    public List<VerticePerimetro> getVertices() { return List.copyOf(vertices); }
    public void atualizar(StatusCrs status, String crs, String datum, String observacao, List<VerticePerimetro> vertices) {
        this.statusCrs = status; this.crs = crs; this.datum = datum; this.observacao = observacao;
        this.vertices.clear(); this.vertices.addAll(vertices);
    }
}
