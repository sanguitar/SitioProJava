package com.example.sitiopro.criacao.peixes.entity;

import com.example.sitiopro.criacao.core.entity.InstalacaoCriacao;
import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "peixes_medicoes_qualidade_agua")
public class MedicaoQualidadeAgua extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "lote_id", nullable = false) private LotePeixes lote;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "instalacao_id", nullable = false) private InstalacaoCriacao instalacao;
    @Column(name = "medido_em", nullable = false) private LocalDateTime medidoEm;
    @Column(nullable = false, precision = 8, scale = 4) private BigDecimal temperatura;
    @Column(nullable = false, precision = 8, scale = 4) private BigDecimal ph;
    @Column(name = "oxigenio_dissolvido", nullable = false, precision = 8, scale = 4) private BigDecimal oxigenioDissolvido;
    @Column(name = "transparencia_cm", precision = 10, scale = 4) private BigDecimal transparenciaCm;
    @Column(precision = 8, scale = 4) private BigDecimal amonia;
    @Column(precision = 8, scale = 4) private BigDecimal nitrito;
    @Column(nullable = false, length = 120) private String responsavel;
    @Column(length = 1000) private String observacao;
    @Column(name = "chave_idempotencia", nullable = false, length = 100, unique = true) private String chaveIdempotencia;
    @Version @Column(nullable = false) private long versao;
    public Long getId(){return id;} public LotePeixes getLote(){return lote;} public void setLote(LotePeixes v){lote=v;}
    public InstalacaoCriacao getInstalacao(){return instalacao;} public void setInstalacao(InstalacaoCriacao v){instalacao=v;}
    public LocalDateTime getMedidoEm(){return medidoEm;} public void setMedidoEm(LocalDateTime v){medidoEm=v;}
    public BigDecimal getTemperatura(){return temperatura;} public void setTemperatura(BigDecimal v){temperatura=v;}
    public BigDecimal getPh(){return ph;} public void setPh(BigDecimal v){ph=v;}
    public BigDecimal getOxigenioDissolvido(){return oxigenioDissolvido;} public void setOxigenioDissolvido(BigDecimal v){oxigenioDissolvido=v;}
    public BigDecimal getTransparenciaCm(){return transparenciaCm;} public void setTransparenciaCm(BigDecimal v){transparenciaCm=v;}
    public BigDecimal getAmonia(){return amonia;} public void setAmonia(BigDecimal v){amonia=v;}
    public BigDecimal getNitrito(){return nitrito;} public void setNitrito(BigDecimal v){nitrito=v;}
    public String getResponsavel(){return responsavel;} public void setResponsavel(String v){responsavel=v;}
    public String getObservacao(){return observacao;} public void setObservacao(String v){observacao=v;}
    public String getChaveIdempotencia(){return chaveIdempotencia;} public void setChaveIdempotencia(String v){chaveIdempotencia=v;}
    public long getVersao(){return versao;}
}
