package com.example.sitiopro.criacao.aves.entity;

import com.example.sitiopro.estoque.entity.ItemEstoque;
import com.example.sitiopro.estoque.entity.LocalEstoque;
import com.example.sitiopro.estoque.entity.MovimentoEstoque;
import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "aves_alimentacoes")
public class AlimentacaoAves extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "lote_id", nullable = false)
    private LoteAves lote;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "item_estoque_id", nullable = false)
    private ItemEstoque itemEstoque;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "local_estoque_id", nullable = false)
    private LocalEstoque localEstoque;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantidade;
    @Column(name = "data_evento", nullable = false)
    private LocalDateTime dataEvento;
    @Column(length = 1000)
    private String observacao;
    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 100)
    private String chaveIdempotencia;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "movimento_estoque_id", unique = true)
    private MovimentoEstoque movimentoEstoque;
    @Column(name = "custo_unitario_referencia", precision = 19, scale = 4)
    private BigDecimal custoUnitarioReferencia;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "evento_id", unique = true)
    private EventoLoteAves evento;

    public Long getId() { return id; }
    public LoteAves getLote() { return lote; }
    public void setLote(LoteAves lote) { this.lote = lote; }
    public ItemEstoque getItemEstoque() { return itemEstoque; }
    public void setItemEstoque(ItemEstoque itemEstoque) { this.itemEstoque = itemEstoque; }
    public LocalEstoque getLocalEstoque() { return localEstoque; }
    public void setLocalEstoque(LocalEstoque localEstoque) { this.localEstoque = localEstoque; }
    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }
    public LocalDateTime getDataEvento() { return dataEvento; }
    public void setDataEvento(LocalDateTime dataEvento) { this.dataEvento = dataEvento; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public MovimentoEstoque getMovimentoEstoque() { return movimentoEstoque; }
    public void setMovimentoEstoque(MovimentoEstoque movimentoEstoque) { this.movimentoEstoque = movimentoEstoque; }
    public BigDecimal getCustoUnitarioReferencia() { return custoUnitarioReferencia; }
    public void setCustoUnitarioReferencia(BigDecimal valor) { this.custoUnitarioReferencia = valor; }
    public EventoLoteAves getEvento() { return evento; }
    public void setEvento(EventoLoteAves evento) { this.evento = evento; }
}
