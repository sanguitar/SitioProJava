package com.example.sitiopro.agricultura.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import com.example.sitiopro.propriedade.entity.Talhao;
import com.example.sitiopro.propriedade.entity.Propriedade;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "agricultura_cultivos")
public class Cultivo extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propriedade_id", nullable = false)
    private Propriedade propriedade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "safra_id", nullable = false)
    private Safra safra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "talhao_id", nullable = false)
    private Talhao talhao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cultura_id", nullable = false)
    private CulturaAgricola cultura;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal areaCultivadaHa;

    @Column(nullable = false)
    private LocalDate dataPlantio;

    private LocalDate previsaoColheita;

    private LocalDate dataColheitaReal;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 25)
    private StatusCultivo status = StatusCultivo.PLANEJADO;

    @Column(nullable = false)
    private long revisaoOperacoes;

    @Column(length = 1000)
    private String observacao;

    @Version @Column(nullable = false)
    private long versao;

    public Long getId() { return id; }
    public Propriedade getPropriedade() { return propriedade; }
    public void setPropriedade(Propriedade propriedade) { this.propriedade = propriedade; }
    public Safra getSafra() { return safra; }
    public void setSafra(Safra safra) { this.safra = safra; }
    public Talhao getTalhao() { return talhao; }
    public void setTalhao(Talhao talhao) { this.talhao = talhao; }
    public CulturaAgricola getCultura() { return cultura; }
    public void setCultura(CulturaAgricola cultura) { this.cultura = cultura; }
    public BigDecimal getAreaCultivadaHa() { return areaCultivadaHa; }
    public void setAreaCultivadaHa(BigDecimal areaCultivadaHa) { this.areaCultivadaHa = areaCultivadaHa; }
    public LocalDate getDataPlantio() { return dataPlantio; }
    public void setDataPlantio(LocalDate dataPlantio) { this.dataPlantio = dataPlantio; }
    public LocalDate getPrevisaoColheita() { return previsaoColheita; }
    public void setPrevisaoColheita(LocalDate previsaoColheita) { this.previsaoColheita = previsaoColheita; }
    public LocalDate getDataColheitaReal() { return dataColheitaReal; }
    public void setDataColheitaReal(LocalDate dataColheitaReal) { this.dataColheitaReal = dataColheitaReal; }
    public StatusCultivo getStatus() { return status; }
    public void setStatus(StatusCultivo status) { this.status = status; }
    public long getRevisaoOperacoes() { return revisaoOperacoes; }
    public void setRevisaoOperacoes(long revisaoOperacoes) { this.revisaoOperacoes = revisaoOperacoes; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public long getVersao() { return versao; }
}
