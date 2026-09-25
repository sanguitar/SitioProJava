package com.example.sitiopro.criacao.suinos.entity;

import com.example.sitiopro.criacao.core.entity.InstalacaoCriacao;
import com.example.sitiopro.estoque.entity.*;
import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "suinos_eventos")
public class EventoSuinos extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "lote_id") private LoteSuinos lote;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private TipoEventoSuinos tipo;
    private Integer quantidade;
    @Column(name = "valor_decimal", precision = 19, scale = 4) private BigDecimal valorDecimal;
    @Column(name = "data_evento", nullable = false) private LocalDateTime dataEvento;
    @Column(nullable = false, length = 100) private String usuario;
    @Column(length = 1000) private String observacao;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "instalacao_origem_id") private InstalacaoCriacao instalacaoOrigem;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "instalacao_destino_id") private InstalacaoCriacao instalacaoDestino;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "item_estoque_id") private ItemEstoque itemEstoque;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "local_estoque_id") private LocalEstoque localEstoque;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "movimento_estoque_id", unique = true) private MovimentoEstoque movimentoEstoque;
    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 100) private String chaveIdempotencia;
    @Version @Column(nullable = false) private long versao;

    public Long getId(){return id;} public LoteSuinos getLote(){return lote;} public void setLote(LoteSuinos v){lote=v;}
    public TipoEventoSuinos getTipo(){return tipo;} public void setTipo(TipoEventoSuinos v){tipo=v;}
    public Integer getQuantidade(){return quantidade;} public void setQuantidade(Integer v){quantidade=v;}
    public BigDecimal getValorDecimal(){return valorDecimal;} public void setValorDecimal(BigDecimal v){valorDecimal=v;}
    public LocalDateTime getDataEvento(){return dataEvento;} public void setDataEvento(LocalDateTime v){dataEvento=v;}
    public String getUsuario(){return usuario;} public void setUsuario(String v){usuario=v;}
    public String getObservacao(){return observacao;} public void setObservacao(String v){observacao=v;}
    public InstalacaoCriacao getInstalacaoOrigem(){return instalacaoOrigem;} public void setInstalacaoOrigem(InstalacaoCriacao v){instalacaoOrigem=v;}
    public InstalacaoCriacao getInstalacaoDestino(){return instalacaoDestino;} public void setInstalacaoDestino(InstalacaoCriacao v){instalacaoDestino=v;}
    public ItemEstoque getItemEstoque(){return itemEstoque;} public void setItemEstoque(ItemEstoque v){itemEstoque=v;}
    public LocalEstoque getLocalEstoque(){return localEstoque;} public void setLocalEstoque(LocalEstoque v){localEstoque=v;}
    public MovimentoEstoque getMovimentoEstoque(){return movimentoEstoque;} public void setMovimentoEstoque(MovimentoEstoque v){movimentoEstoque=v;}
    public String getChaveIdempotencia(){return chaveIdempotencia;} public void setChaveIdempotencia(String v){chaveIdempotencia=v;}
    public long getVersao(){return versao;}
}
