package com.example.sitiopro.propriedade.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
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
    @Column(length = 1000) private String observacao;
    @Version @Column(nullable = false) private long versao;
    @ElementCollection
    @CollectionTable(name = "propriedade_perimetro_vertices", joinColumns = @JoinColumn(name = "perimetro_id"))
    @OrderBy("ordem ASC")
    private List<VerticePerimetro> vertices = new ArrayList<>();

    public Long getId() { return id; }
    public void setPropriedade(Propriedade propriedade) { this.propriedade = propriedade; }
    public StatusCrs getStatusCrs() { return statusCrs; }
    public String getCrs() { return crs; }
    public String getDatum() { return datum; }
    public String getObservacao() { return observacao; }
    public long getVersao() { return versao; }
    public List<VerticePerimetro> getVertices() { return List.copyOf(vertices); }
    public void atualizar(StatusCrs status, String crs, String datum, String observacao, List<VerticePerimetro> vertices) {
        this.statusCrs = status; this.crs = crs; this.datum = datum; this.observacao = observacao;
        this.vertices.clear(); this.vertices.addAll(vertices);
    }
}
