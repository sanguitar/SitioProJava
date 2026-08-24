package com.example.sitiopro.criacao.aves.entity;

import com.example.sitiopro.criacao.core.entity.InstalacaoCriacao;
import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "aves_lotes")
public class LoteAves extends AuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 80)
    private String codigo;
    @Column(length = 120)
    private String nome;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private EspecieAves especie;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private FinalidadeLoteAves finalidade;
    @Column(length = 120)
    private String linhagem;
    @Column(nullable = false, length = 200)
    private String origem;
    @Column(name = "data_entrada", nullable = false)
    private LocalDate dataEntrada;
    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;
    @Column(name = "quantidade_inicial", nullable = false)
    private int quantidadeInicial;
    @Column(name = "quantidade_atual", nullable = false)
    private int quantidadeAtual;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private SexoLoteAves sexo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instalacao_atual_id", nullable = false)
    private InstalacaoCriacao instalacaoAtual;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private StatusLoteAves status;
    @Column(length = 1000)
    private String observacoes;
    @Column(name = "custo_inicial", precision = 19, scale = 4)
    private BigDecimal custoInicial;
    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 100)
    private String chaveIdempotencia;
    @Version @Column(nullable = false)
    private long versao;

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public EspecieAves getEspecie() { return especie; }
    public void setEspecie(EspecieAves especie) { this.especie = especie; }
    public FinalidadeLoteAves getFinalidade() { return finalidade; }
    public void setFinalidade(FinalidadeLoteAves finalidade) { this.finalidade = finalidade; }
    public String getLinhagem() { return linhagem; }
    public void setLinhagem(String linhagem) { this.linhagem = linhagem; }
    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }
    public LocalDate getDataEntrada() { return dataEntrada; }
    public void setDataEntrada(LocalDate dataEntrada) { this.dataEntrada = dataEntrada; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public int getQuantidadeInicial() { return quantidadeInicial; }
    public void setQuantidadeInicial(int quantidadeInicial) { this.quantidadeInicial = quantidadeInicial; }
    public int getQuantidadeAtual() { return quantidadeAtual; }
    public void setQuantidadeAtual(int quantidadeAtual) { this.quantidadeAtual = quantidadeAtual; }
    public SexoLoteAves getSexo() { return sexo; }
    public void setSexo(SexoLoteAves sexo) { this.sexo = sexo; }
    public InstalacaoCriacao getInstalacaoAtual() { return instalacaoAtual; }
    public void setInstalacaoAtual(InstalacaoCriacao instalacaoAtual) { this.instalacaoAtual = instalacaoAtual; }
    public StatusLoteAves getStatus() { return status; }
    public void setStatus(StatusLoteAves status) { this.status = status; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public BigDecimal getCustoInicial() { return custoInicial; }
    public void setCustoInicial(BigDecimal custoInicial) { this.custoInicial = custoInicial; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public long getVersao() { return versao; }
}
