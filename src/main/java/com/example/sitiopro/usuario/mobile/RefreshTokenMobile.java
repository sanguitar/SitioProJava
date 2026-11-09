package com.example.sitiopro.usuario.mobile;

import com.example.sitiopro.usuario.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "mobile_refresh_tokens")
public class RefreshTokenMobile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sessao_id", nullable = false, length = 36)
    private String sessaoId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "revogado_em")
    private LocalDateTime revogadoEm;

    @Column(name = "motivo_revogacao", length = 30)
    private String motivoRevogacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substituido_por_id")
    private RefreshTokenMobile substituidoPor;

    @Version
    @Column(name = "versao", nullable = false)
    private long versao;

    protected RefreshTokenMobile() {
    }

    public RefreshTokenMobile(String sessaoId, String tokenHash, Usuario usuario,
            LocalDateTime criadoEm, LocalDateTime expiraEm) {
        this.sessaoId = sessaoId;
        this.tokenHash = tokenHash;
        this.usuario = usuario;
        this.criadoEm = criadoEm;
        this.expiraEm = expiraEm;
    }

    public void revogar(LocalDateTime instante, String motivo, RefreshTokenMobile substituto) {
        if (revogadoEm == null) {
            revogadoEm = instante;
            motivoRevogacao = motivo;
            substituidoPor = substituto;
        }
    }

    public boolean estaAtivo(LocalDateTime agora) {
        return revogadoEm == null && expiraEm.isAfter(agora) && usuario.isAtivo();
    }

    public Long getId() { return id; }
    public String getSessaoId() { return sessaoId; }
    public String getTokenHash() { return tokenHash; }
    public Usuario getUsuario() { return usuario; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getExpiraEm() { return expiraEm; }
    public LocalDateTime getRevogadoEm() { return revogadoEm; }
    public String getMotivoRevogacao() { return motivoRevogacao; }
    public RefreshTokenMobile getSubstituidoPor() { return substituidoPor; }
    public long getVersao() { return versao; }
}
