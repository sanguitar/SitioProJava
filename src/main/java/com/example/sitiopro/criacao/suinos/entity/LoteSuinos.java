package com.example.sitiopro.criacao.suinos.entity;

import com.example.sitiopro.criacao.core.entity.InstalacaoCriacao;
import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "suinos_lotes")
public class LoteSuinos extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 80) private String codigo;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private CategoriaSuino categoria;
    @Column(name = "data_entrada", nullable = false) private LocalDate dataEntrada;
    @Column(name = "data_nascimento") private LocalDate dataNascimento;
    @Column(nullable = false, length = 200) private String origem;
    @Column(name = "quantidade_inicial", nullable = false) private int quantidadeInicial;
    @Column(name = "quantidade_atual", nullable = false) private int quantidadeAtual;
    @Column(name = "peso_medio", precision = 19, scale = 4) private BigDecimal pesoMedio;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "instalacao_atual_id") private InstalacaoCriacao instalacaoAtual;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private StatusLoteSuinos status;
    @Column(length = 1000) private String observacao;
    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 100) private String chaveIdempotencia;
    @Version @Column(nullable = false) private long versao;

    public Long getId() { return id; } public String getCodigo() { return codigo; } public void setCodigo(String v) { codigo=v; }
    public CategoriaSuino getCategoria() { return categoria; } public void setCategoria(CategoriaSuino v) { categoria=v; }
    public LocalDate getDataEntrada() { return dataEntrada; } public void setDataEntrada(LocalDate v) { dataEntrada=v; }
    public LocalDate getDataNascimento() { return dataNascimento; } public void setDataNascimento(LocalDate v) { dataNascimento=v; }
    public String getOrigem() { return origem; } public void setOrigem(String v) { origem=v; }
    public int getQuantidadeInicial() { return quantidadeInicial; } public void setQuantidadeInicial(int v) { quantidadeInicial=v; }
    public int getQuantidadeAtual() { return quantidadeAtual; } public void setQuantidadeAtual(int v) { quantidadeAtual=v; }
    public BigDecimal getPesoMedio() { return pesoMedio; } public void setPesoMedio(BigDecimal v) { pesoMedio=v; }
    public InstalacaoCriacao getInstalacaoAtual() { return instalacaoAtual; } public void setInstalacaoAtual(InstalacaoCriacao v) { instalacaoAtual=v; }
    public StatusLoteSuinos getStatus() { return status; } public void setStatus(StatusLoteSuinos v) { status=v; }
    public String getObservacao() { return observacao; } public void setObservacao(String v) { observacao=v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia=v; }
    public long getVersao() { return versao; }
}
