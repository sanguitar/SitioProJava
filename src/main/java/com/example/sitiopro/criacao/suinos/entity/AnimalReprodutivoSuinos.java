package com.example.sitiopro.criacao.suinos.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "suinos_animais_reprodutivos")
public class AnimalReprodutivoSuinos extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 80) private String codigo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "lote_id") private LoteSuinos lote;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TipoAnimalReprodutivo tipo;
    @Column(length = 120) private String identificacao;
    @Column(name = "data_nascimento") private LocalDate dataNascimento;
    @Column(name = "peso_atual", precision = 19, scale = 4) private BigDecimal pesoAtual;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private StatusAnimalReprodutivo status;
    @Column(length = 1000) private String observacao;
    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 100) private String chaveIdempotencia;
    @Version @Column(nullable = false) private long versao;

    public Long getId() { return id; }
    public String getCodigo() { return codigo; } public void setCodigo(String codigo) { this.codigo = codigo; }
    public LoteSuinos getLote() { return lote; } public void setLote(LoteSuinos lote) { this.lote = lote; }
    public TipoAnimalReprodutivo getTipo() { return tipo; } public void setTipo(TipoAnimalReprodutivo tipo) { this.tipo = tipo; }
    public String getIdentificacao() { return identificacao; } public void setIdentificacao(String identificacao) { this.identificacao = identificacao; }
    public LocalDate getDataNascimento() { return dataNascimento; } public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public BigDecimal getPesoAtual() { return pesoAtual; } public void setPesoAtual(BigDecimal pesoAtual) { this.pesoAtual = pesoAtual; }
    public StatusAnimalReprodutivo getStatus() { return status; } public void setStatus(StatusAnimalReprodutivo status) { this.status = status; }
    public String getObservacao() { return observacao; } public void setObservacao(String observacao) { this.observacao = observacao; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public long getVersao() { return versao; }
}
