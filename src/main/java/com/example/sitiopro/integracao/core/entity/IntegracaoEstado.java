package com.example.sitiopro.integracao.core.entity;

import com.example.sitiopro.integracao.core.FonteIntegracao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;

@Entity
@Table(name = "integracao_estados")
public class IntegracaoEstado {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private FonteIntegracao fonte;

    @Column(name = "ultimo_sucesso_em")
    private LocalDateTime ultimoSucessoEm;

    @Column(name = "ultima_tentativa_em")
    private LocalDateTime ultimaTentativaEm;

    @Column(name = "em_execucao", nullable = false)
    private boolean emExecucao;

    @Column(name = "execucao_iniciada_em")
    private LocalDateTime execucaoIniciadaEm;

    @Column(name = "checkpoint_valor", length = 500)
    private String checkpoint;

    @Column(length = 255)
    private String etag;

    @Column(name = "last_modified", length = 255)
    private String lastModified;

    @Version
    @Column(nullable = false)
    private long versao;

    protected IntegracaoEstado() {
    }

    public IntegracaoEstado(FonteIntegracao fonte) {
        this.fonte = fonte;
    }

    public FonteIntegracao getFonte() {
        return fonte;
    }

    public LocalDateTime getUltimoSucessoEm() {
        return ultimoSucessoEm;
    }

    public void setUltimoSucessoEm(LocalDateTime ultimoSucessoEm) {
        this.ultimoSucessoEm = ultimoSucessoEm;
    }

    public LocalDateTime getUltimaTentativaEm() {
        return ultimaTentativaEm;
    }

    public void setUltimaTentativaEm(LocalDateTime ultimaTentativaEm) {
        this.ultimaTentativaEm = ultimaTentativaEm;
    }

    public boolean isEmExecucao() {
        return emExecucao;
    }

    public void setEmExecucao(boolean emExecucao) {
        this.emExecucao = emExecucao;
    }

    public LocalDateTime getExecucaoIniciadaEm() {
        return execucaoIniciadaEm;
    }

    public void setExecucaoIniciadaEm(LocalDateTime execucaoIniciadaEm) {
        this.execucaoIniciadaEm = execucaoIniciadaEm;
    }

    public String getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(String checkpoint) {
        this.checkpoint = checkpoint;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
}
