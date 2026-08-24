package com.example.sitiopro.tarefas.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import com.example.sitiopro.usuario.entity.Usuario;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;

@Entity
@Table(name = "alertas")
public class Alerta extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String titulo;

    @Column(length = 2000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeveridadeAlerta severidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAlerta status;

    @Enumerated(EnumType.STRING)
    @Column(name = "modulo_origem", nullable = false, length = 30)
    private ModuloOrigem moduloOrigem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoAlerta tipo;

    @Column(name = "referencia_origem", nullable = false, length = 160)
    private String referenciaOrigem;

    @Column(name = "chave_deduplicacao", nullable = false, length = 220)
    private String chaveDeduplicacao;

    @Column(name = "detectado_em", nullable = false)
    private LocalDateTime detectadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @Column(name = "resolvido_em")
    private LocalDateTime resolvidoEm;

    @Column(name = "reconhecido_em")
    private LocalDateTime reconhecidoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconhecido_por_id")
    private Usuario reconhecidoPor;

    @Column(name = "dados_contexto", length = 1000)
    private String dadosContexto;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_id", unique = true)
    private Tarefa tarefa;

    @Version
    @Column(name = "versao", nullable = false)
    private long versao;

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public SeveridadeAlerta getSeveridade() {
        return severidade;
    }

    public void setSeveridade(SeveridadeAlerta severidade) {
        this.severidade = severidade;
    }

    public StatusAlerta getStatus() {
        return status;
    }

    public void setStatus(StatusAlerta status) {
        this.status = status;
    }

    public ModuloOrigem getModuloOrigem() {
        return moduloOrigem;
    }

    public void setModuloOrigem(ModuloOrigem moduloOrigem) {
        this.moduloOrigem = moduloOrigem;
    }

    public TipoAlerta getTipo() {
        return tipo;
    }

    public void setTipo(TipoAlerta tipo) {
        this.tipo = tipo;
    }

    public String getReferenciaOrigem() {
        return referenciaOrigem;
    }

    public void setReferenciaOrigem(String referenciaOrigem) {
        this.referenciaOrigem = referenciaOrigem;
    }

    public String getChaveDeduplicacao() {
        return chaveDeduplicacao;
    }

    public void setChaveDeduplicacao(String chaveDeduplicacao) {
        this.chaveDeduplicacao = chaveDeduplicacao;
    }

    public LocalDateTime getDetectadoEm() {
        return detectadoEm;
    }

    public void setDetectadoEm(LocalDateTime detectadoEm) {
        this.detectadoEm = detectadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public LocalDateTime getResolvidoEm() {
        return resolvidoEm;
    }

    public void setResolvidoEm(LocalDateTime resolvidoEm) {
        this.resolvidoEm = resolvidoEm;
    }

    public LocalDateTime getReconhecidoEm() {
        return reconhecidoEm;
    }

    public void setReconhecidoEm(LocalDateTime reconhecidoEm) {
        this.reconhecidoEm = reconhecidoEm;
    }

    public Usuario getReconhecidoPor() {
        return reconhecidoPor;
    }

    public void setReconhecidoPor(Usuario reconhecidoPor) {
        this.reconhecidoPor = reconhecidoPor;
    }

    public String getDadosContexto() {
        return dadosContexto;
    }

    public void setDadosContexto(String dadosContexto) {
        this.dadosContexto = dadosContexto;
    }

    public Tarefa getTarefa() {
        return tarefa;
    }

    public void setTarefa(Tarefa tarefa) {
        this.tarefa = tarefa;
    }

    public long getVersao() {
        return versao;
    }
}
