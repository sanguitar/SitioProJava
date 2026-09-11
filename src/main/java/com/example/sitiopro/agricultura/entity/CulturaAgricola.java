package com.example.sitiopro.agricultura.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import com.example.sitiopro.integracao.embrapa.agrofit.entity.AgrofitCultura;

@Entity
@Table(name = "agricultura_culturas")
public class CulturaAgricola extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String nomeComum;

    @Column(length = 180)
    private String nomeCientifico;

    private Integer cicloDiasEstimado;

    @Column(nullable = false)
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "agrofit_cultura_id")
    private AgrofitCultura agrofitCultura;

    @Column(length = 1000)
    private String observacao;

    @Version @Column(nullable = false)
    private long versao;

    public Long getId() { return id; }
    public String getNomeComum() { return nomeComum; }
    public void setNomeComum(String nomeComum) { this.nomeComum = nomeComum; }
    public String getNomeCientifico() { return nomeCientifico; }
    public void setNomeCientifico(String nomeCientifico) { this.nomeCientifico = nomeCientifico; }
    public Integer getCicloDiasEstimado() { return cicloDiasEstimado; }
    public void setCicloDiasEstimado(Integer cicloDiasEstimado) { this.cicloDiasEstimado = cicloDiasEstimado; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public AgrofitCultura getAgrofitCultura() { return agrofitCultura; }
    public void setAgrofitCultura(AgrofitCultura agrofitCultura) { this.agrofitCultura = agrofitCultura; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public long getVersao() { return versao; }
}
