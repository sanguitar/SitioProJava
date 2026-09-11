package com.example.sitiopro.agricultura.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import com.example.sitiopro.estoque.entity.MovimentoEstoque;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "agricultura_colheitas")
public class Colheita extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cultivo_id", nullable = false)
    private Cultivo cultivo;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantidade;

    @Column(nullable = false, length = 30)
    private String unidade;

    @Column(length = 120)
    private String classificacao;

    @Column(precision = 18, scale = 4)
    private BigDecimal perdas;

    @Column(nullable = false)
    private boolean finalizaCultivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DestinoColheita destino = DestinoColheita.SEM_ESTOQUE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimento_estoque_id")
    private MovimentoEstoque movimentoEstoque;

    @Column(name = "chave_idempotencia", length = 80)
    private String chaveIdempotencia;

    @Column(length = 1000)
    private String observacao;

    public Long getId() { return id; }
    public Cultivo getCultivo() { return cultivo; }
    public void setCultivo(Cultivo cultivo) { this.cultivo = cultivo; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }
    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }
    public String getClassificacao() { return classificacao; }
    public void setClassificacao(String classificacao) { this.classificacao = classificacao; }
    public BigDecimal getPerdas() { return perdas; }
    public void setPerdas(BigDecimal perdas) { this.perdas = perdas; }
    public boolean isFinalizaCultivo() { return finalizaCultivo; }
    public void setFinalizaCultivo(boolean finalizaCultivo) { this.finalizaCultivo = finalizaCultivo; }
    public DestinoColheita getDestino() { return destino; }
    public void setDestino(DestinoColheita destino) { this.destino = destino; }
    public MovimentoEstoque getMovimentoEstoque() { return movimentoEstoque; }
    public void setMovimentoEstoque(MovimentoEstoque movimentoEstoque) { this.movimentoEstoque = movimentoEstoque; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
