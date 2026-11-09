CREATE TABLE dbo.mobile_refresh_tokens (
    id BIGINT IDENTITY(1,1) NOT NULL,
    sessao_id VARCHAR(36) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    usuario_id BIGINT NOT NULL,
    criado_em DATETIME2(7) NOT NULL,
    expira_em DATETIME2(7) NOT NULL,
    revogado_em DATETIME2(7) NULL,
    motivo_revogacao VARCHAR(30) NULL,
    substituido_por_id BIGINT NULL,
    versao BIGINT NOT NULL CONSTRAINT df_mobile_refresh_tokens_versao DEFAULT 0,
    CONSTRAINT pk_mobile_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uq_mobile_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_mobile_refresh_tokens_usuario FOREIGN KEY (usuario_id) REFERENCES dbo.usuarios(id),
    CONSTRAINT fk_mobile_refresh_tokens_substituto FOREIGN KEY (substituido_por_id)
        REFERENCES dbo.mobile_refresh_tokens(id),
    CONSTRAINT ck_mobile_refresh_tokens_datas CHECK (expira_em > criado_em),
    CONSTRAINT ck_mobile_refresh_tokens_revogacao CHECK (
        (revogado_em IS NULL AND motivo_revogacao IS NULL)
        OR (revogado_em IS NOT NULL AND motivo_revogacao IS NOT NULL)
    )
);

CREATE INDEX ix_mobile_refresh_tokens_sessao
    ON dbo.mobile_refresh_tokens(sessao_id, revogado_em, expira_em);

CREATE INDEX ix_mobile_refresh_tokens_usuario
    ON dbo.mobile_refresh_tokens(usuario_id, revogado_em, expira_em);
