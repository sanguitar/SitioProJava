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
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;

@Entity
@Table(name = "tarefas")
public class Tarefa extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String titulo;

    @Column(length = 2000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusTarefa status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrioridadeTarefa prioridade;

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_vencimento")
    private LocalDateTime dataVencimento;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criado_por_usuario_id", nullable = false)
    private Usuario criadoPorUsuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigemTarefa origem;

    @Enumerated(EnumType.STRING)
    @Column(name = "modulo_origem", length = 30)
    private ModuloOrigem moduloOrigem;

    @Column(name = "referencia_origem", length = 160)
    private String referenciaOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorrencia_id")
    private TarefaRecorrencia recorrenciaOrigem;

    @Column(name = "ocorrencia_programada_em")
    private LocalDateTime ocorrenciaProgramadaEm;

    @Column(nullable = false)
    private boolean ativo = true;

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

    public StatusTarefa getStatus() {
        return status;
    }

    public void setStatus(StatusTarefa status) {
        this.status = status;
    }

    public PrioridadeTarefa getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(PrioridadeTarefa prioridade) {
        this.prioridade = prioridade;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDateTime dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public LocalDateTime getDataConclusao() {
        return dataConclusao;
    }

    public void setDataConclusao(LocalDateTime dataConclusao) {
        this.dataConclusao = dataConclusao;
    }

    public Usuario getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(Usuario responsavel) {
        this.responsavel = responsavel;
    }

    public Usuario getCriadoPorUsuario() {
        return criadoPorUsuario;
    }

    public void setCriadoPorUsuario(Usuario criadoPorUsuario) {
        this.criadoPorUsuario = criadoPorUsuario;
    }

    public OrigemTarefa getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemTarefa origem) {
        this.origem = origem;
    }

    public ModuloOrigem getModuloOrigem() {
        return moduloOrigem;
    }

    public void setModuloOrigem(ModuloOrigem moduloOrigem) {
        this.moduloOrigem = moduloOrigem;
    }

    public String getReferenciaOrigem() {
        return referenciaOrigem;
    }

    public void setReferenciaOrigem(String referenciaOrigem) {
        this.referenciaOrigem = referenciaOrigem;
    }

    public TarefaRecorrencia getRecorrenciaOrigem() {
        return recorrenciaOrigem;
    }

    public void setRecorrenciaOrigem(TarefaRecorrencia recorrenciaOrigem) {
        this.recorrenciaOrigem = recorrenciaOrigem;
    }

    public LocalDateTime getOcorrenciaProgramadaEm() {
        return ocorrenciaProgramadaEm;
    }

    public void setOcorrenciaProgramadaEm(LocalDateTime ocorrenciaProgramadaEm) {
        this.ocorrenciaProgramadaEm = ocorrenciaProgramadaEm;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public long getVersao() {
        return versao;
    }
}
