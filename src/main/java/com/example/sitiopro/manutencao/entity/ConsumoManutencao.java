package com.example.sitiopro.manutencao.entity;

import com.example.sitiopro.estoque.entity.*;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "manutencao_consumos")
public class ConsumoManutencao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "manutencao_id", nullable = false) private RegistroManutencao manutencao;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "item_estoque_id", nullable = false) private ItemEstoque item;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "local_estoque_id", nullable = false) private LocalEstoque local;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal quantidade;
    @Column(name = "lote_estoque_codigo", length = 100) private String loteEstoqueCodigo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "movimento_estoque_id", nullable = false) private MovimentoEstoque movimentoEstoque;

    public Long getId() { return id; }
    public RegistroManutencao getManutencao() { return manutencao; } public void setManutencao(RegistroManutencao v) { manutencao = v; }
    public ItemEstoque getItem() { return item; } public void setItem(ItemEstoque v) { item = v; }
    public LocalEstoque getLocal() { return local; } public void setLocal(LocalEstoque v) { local = v; }
    public BigDecimal getQuantidade() { return quantidade; } public void setQuantidade(BigDecimal v) { quantidade = v; }
    public String getLoteEstoqueCodigo() { return loteEstoqueCodigo; } public void setLoteEstoqueCodigo(String v) { loteEstoqueCodigo = v; }
    public MovimentoEstoque getMovimentoEstoque() { return movimentoEstoque; } public void setMovimentoEstoque(MovimentoEstoque v) { movimentoEstoque = v; }
}
