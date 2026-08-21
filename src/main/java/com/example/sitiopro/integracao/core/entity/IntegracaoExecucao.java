package com.example.sitiopro.integracao.core.entity;

import com.example.sitiopro.integracao.core.FonteIntegracao;
import com.example.sitiopro.integracao.core.StatusExecucaoIntegracao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "integracao_execucoes")
public class IntegracaoExecucao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FonteIntegracao fonte;

    @Column(name = "iniciado_em", nullable = false)
    private LocalDateTime iniciadoEm;

    @Column(name = "finalizado_em")
    private LocalDateTime finalizadoEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusExecucaoIntegracao status;

    @Column(name = "registros_lidos", nullable = false)
    private int registrosLidos;

    @Column(name = "registros_inseridos", nullable = false)
    private int registrosInseridos;

    @Column(name = "registros_atualizados", nullable = false)
    private int registrosAtualizados;

    @Column(name = "registros_ignorados", nullable = false)
    private int registrosIgnorados;

    @Column(name = "erro_codigo", length = 80)
    private String erroCodigo;

    @Column(name = "erro_resumo", length = 500)
    private String erroResumo;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    protected IntegracaoExecucao() {
    }

    public IntegracaoExecucao(FonteIntegracao fonte, LocalDateTime iniciadoEm, String traceId) {
        this.fonte = fonte;
        this.iniciadoEm = iniciadoEm;
        this.traceId = traceId;
        this.status = StatusExecucaoIntegracao.RUNNING;
    }

    public Long getId() {
        return id;
    }

    public FonteIntegracao getFonte() {
        return fonte;
    }

    public LocalDateTime getIniciadoEm() {
        return iniciadoEm;
    }

    public LocalDateTime getFinalizadoEm() {
        return finalizadoEm;
    }

    public void setFinalizadoEm(LocalDateTime finalizadoEm) {
        this.finalizadoEm = finalizadoEm;
    }

    public StatusExecucaoIntegracao getStatus() {
        return status;
    }

    public void setStatus(StatusExecucaoIntegracao status) {
        this.status = status;
    }

    public int getRegistrosLidos() {
        return registrosLidos;
    }

    public void setRegistrosLidos(int registrosLidos) {
        this.registrosLidos = registrosLidos;
    }

    public int getRegistrosInseridos() {
        return registrosInseridos;
    }

    public void setRegistrosInseridos(int registrosInseridos) {
        this.registrosInseridos = registrosInseridos;
    }

    public int getRegistrosAtualizados() {
        return registrosAtualizados;
    }

    public void setRegistrosAtualizados(int registrosAtualizados) {
        this.registrosAtualizados = registrosAtualizados;
    }

    public int getRegistrosIgnorados() {
        return registrosIgnorados;
    }

    public void setRegistrosIgnorados(int registrosIgnorados) {
        this.registrosIgnorados = registrosIgnorados;
    }

    public String getErroCodigo() {
        return erroCodigo;
    }

    public void setErroCodigo(String erroCodigo) {
        this.erroCodigo = erroCodigo;
    }

    public String getErroResumo() {
        return erroResumo;
    }

    public void setErroResumo(String erroResumo) {
        this.erroResumo = erroResumo;
    }

    public String getTraceId() {
        return traceId;
    }
}
