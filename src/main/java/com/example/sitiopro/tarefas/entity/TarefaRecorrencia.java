package com.example.sitiopro.tarefas.entity;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;

@Entity
@Table(name = "tarefa_recorrencias")
public class TarefaRecorrencia extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tarefa_modelo_id", nullable = false, unique = true)
    private Tarefa tarefaModelo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoRecorrencia tipo;

    @Column(name = "intervalo_dias")
    private Integer intervaloDias;

    @Column(name = "proxima_ocorrencia_em", nullable = false)
    private LocalDateTime proximaOcorrenciaEm;

    @Column(nullable = false)
    private boolean ativa = true;

    @Version
    @Column(name = "versao", nullable = false)
    private long versao;

    public Long getId() {
        return id;
    }

    public Tarefa getTarefaModelo() {
        return tarefaModelo;
    }

    public void setTarefaModelo(Tarefa tarefaModelo) {
        this.tarefaModelo = tarefaModelo;
    }

    public TipoRecorrencia getTipo() {
        return tipo;
    }

    public void setTipo(TipoRecorrencia tipo) {
        this.tipo = tipo;
    }

    public Integer getIntervaloDias() {
        return intervaloDias;
    }

    public void setIntervaloDias(Integer intervaloDias) {
        this.intervaloDias = intervaloDias;
    }

    public LocalDateTime getProximaOcorrenciaEm() {
        return proximaOcorrenciaEm;
    }

    public void setProximaOcorrenciaEm(LocalDateTime proximaOcorrenciaEm) {
        this.proximaOcorrenciaEm = proximaOcorrenciaEm;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public long getVersao() {
        return versao;
    }
}
