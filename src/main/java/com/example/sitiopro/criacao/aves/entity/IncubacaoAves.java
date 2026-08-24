package com.example.sitiopro.criacao.aves.entity;

import com.example.sitiopro.criacao.core.entity.InstalacaoCriacao;
import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "aves_incubacoes")
public class IncubacaoAves extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 80)
    private String codigo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "instalacao_id", nullable = false)
    private InstalacaoCriacao instalacao;
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;
    @Column(name = "quantidade_ovos", nullable = false)
    private int quantidadeOvos;
    @Column(name = "origem_ovos", length = 200)
    private String origemOvos;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lote_reprodutor_id")
    private LoteAves loteReprodutor;
    @Column(name = "data_prevista_eclosao", nullable = false)
    private LocalDate dataPrevistaEclosao;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private StatusIncubacaoAves status;
    @Column(length = 1000)
    private String observacao;
    @Column(name = "pintinhos_eclodidos")
    private Integer pintinhosEclodidos;
    @Column(name = "ovos_perdidos")
    private Integer ovosPerdidos;
    @Column(name = "data_eclosao")
    private LocalDate dataEclosao;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lote_resultante_id", unique = true)
    private LoteAves loteResultante;
    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 100)
    private String chaveIdempotencia;
    @Version @Column(nullable = false)
    private long versao;

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public InstalacaoCriacao getInstalacao() { return instalacao; }
    public void setInstalacao(InstalacaoCriacao instalacao) { this.instalacao = instalacao; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public int getQuantidadeOvos() { return quantidadeOvos; }
    public void setQuantidadeOvos(int quantidadeOvos) { this.quantidadeOvos = quantidadeOvos; }
    public String getOrigemOvos() { return origemOvos; }
    public void setOrigemOvos(String origemOvos) { this.origemOvos = origemOvos; }
    public LoteAves getLoteReprodutor() { return loteReprodutor; }
    public void setLoteReprodutor(LoteAves loteReprodutor) { this.loteReprodutor = loteReprodutor; }
    public LocalDate getDataPrevistaEclosao() { return dataPrevistaEclosao; }
    public void setDataPrevistaEclosao(LocalDate valor) { this.dataPrevistaEclosao = valor; }
    public StatusIncubacaoAves getStatus() { return status; }
    public void setStatus(StatusIncubacaoAves status) { this.status = status; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public Integer getPintinhosEclodidos() { return pintinhosEclodidos; }
    public void setPintinhosEclodidos(Integer valor) { this.pintinhosEclodidos = valor; }
    public Integer getOvosPerdidos() { return ovosPerdidos; }
    public void setOvosPerdidos(Integer valor) { this.ovosPerdidos = valor; }
    public LocalDate getDataEclosao() { return dataEclosao; }
    public void setDataEclosao(LocalDate dataEclosao) { this.dataEclosao = dataEclosao; }
    public LoteAves getLoteResultante() { return loteResultante; }
    public void setLoteResultante(LoteAves loteResultante) { this.loteResultante = loteResultante; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public long getVersao() { return versao; }
}
