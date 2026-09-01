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
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private MetodoIncubacaoAves metodo;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private EspecieAves especie;
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;
    @Column(name = "quantidade_ovos", nullable = false)
    private int quantidadeOvos;
    @Column(name = "origem_ovos", length = 200)
    private String origemOvos;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lote_reprodutor_id")
    private LoteAves loteReprodutor;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "postura_origem_id")
    private RegistroPosturaAves posturaOrigem;
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
    @Column(name = "observacao_finalizacao", length = 1000)
    private String observacaoFinalizacao;
    @Column(name = "motivo_ajuste_previsao", length = 500)
    private String motivoAjustePrevisao;
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
    public MetodoIncubacaoAves getMetodo() { return metodo; }
    public void setMetodo(MetodoIncubacaoAves metodo) { this.metodo = metodo; }
    public EspecieAves getEspecie() { return especie; }
    public void setEspecie(EspecieAves especie) { this.especie = especie; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public int getQuantidadeOvos() { return quantidadeOvos; }
    public void setQuantidadeOvos(int quantidadeOvos) { this.quantidadeOvos = quantidadeOvos; }
    public String getOrigemOvos() { return origemOvos; }
    public void setOrigemOvos(String origemOvos) { this.origemOvos = origemOvos; }
    public LoteAves getLoteReprodutor() { return loteReprodutor; }
    public void setLoteReprodutor(LoteAves loteReprodutor) { this.loteReprodutor = loteReprodutor; }
    public RegistroPosturaAves getPosturaOrigem() { return posturaOrigem; }
    public void setPosturaOrigem(RegistroPosturaAves posturaOrigem) { this.posturaOrigem = posturaOrigem; }
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
    public String getObservacaoFinalizacao() { return observacaoFinalizacao; }
    public void setObservacaoFinalizacao(String observacaoFinalizacao) { this.observacaoFinalizacao = observacaoFinalizacao; }
    public String getMotivoAjustePrevisao() { return motivoAjustePrevisao; }
    public void setMotivoAjustePrevisao(String motivoAjustePrevisao) { this.motivoAjustePrevisao = motivoAjustePrevisao; }
    public LoteAves getLoteResultante() { return loteResultante; }
    public void setLoteResultante(LoteAves loteResultante) { this.loteResultante = loteResultante; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public long getVersao() { return versao; }
}
