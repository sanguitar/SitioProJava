IF OBJECT_ID(N'dbo.usuarios', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.usuarios (
        id BIGINT IDENTITY(1,1) NOT NULL,
        nome VARCHAR(120) NOT NULL,
        login VARCHAR(80) NOT NULL,
        senha_hash VARCHAR(255) NOT NULL,
        perfil VARCHAR(20) NOT NULL,
        ativo BIT NOT NULL CONSTRAINT df_usuarios_ativo DEFAULT 1,
        ultimo_login_em DATETIME2 NULL,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_usuarios PRIMARY KEY (id),
        CONSTRAINT uk_usuarios_login UNIQUE (login),
        CONSTRAINT ck_usuarios_perfil CHECK (perfil IN ('ADMIN', 'OPERADOR'))
    );
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'ix_usuarios_login'
      AND object_id = OBJECT_ID(N'dbo.usuarios')
)
BEGIN
    CREATE INDEX ix_usuarios_login ON dbo.usuarios (login);
END
GO
