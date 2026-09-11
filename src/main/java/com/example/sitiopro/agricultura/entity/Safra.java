package com.example.sitiopro.agricultura.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import com.example.sitiopro.propriedade.entity.Propriedade;
import java.time.LocalDate;

@Entity
@Table(name = "agricultura_safras")
public class Safra extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "propriedade_id", nullable = false)
    private Propriedade propriedade;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false)
    private Integer anoInicio;

    @Column(nullable = false)
    private Integer anoFim;

    @Column(nullable = false)
    private LocalDate dataInicio;

    private LocalDate dataFim;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 25)
    private StatusSafra status = StatusSafra.PLANEJADA;

    @Column(length = 1000)
    private String observacao;

    @Version @Column(nullable = false)
    private long versao;

    public Long getId() { return id; }
    public Propriedade getPropriedade() { return propriedade; }
    public void setPropriedade(Propriedade propriedade) { this.propriedade = propriedade; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Integer getAnoInicio() { return anoInicio; }
    public void setAnoInicio(Integer anoInicio) { this.anoInicio = anoInicio; }
    public Integer getAnoFim() { return anoFim; }
    public void setAnoFim(Integer anoFim) { this.anoFim = anoFim; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
    public StatusSafra getStatus() { return status; }
    public void setStatus(StatusSafra status) { this.status = status; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public long getVersao() { return versao; }
}
