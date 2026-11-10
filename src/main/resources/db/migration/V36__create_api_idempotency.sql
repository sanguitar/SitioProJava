CREATE TABLE dbo.api_idempotency (
    id BIGINT IDENTITY(1,1) NOT NULL,
    operation_id UNIQUEIDENTIFIER NOT NULL,
    device_id UNIQUEIDENTIFIER NOT NULL,
    usuario_id BIGINT NOT NULL,
    operacao VARCHAR(80) NOT NULL,
    rota VARCHAR(160) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    http_status INT NOT NULL,
    response_json NVARCHAR(MAX) NOT NULL,
    criado_em DATETIMEOFFSET(6) NOT NULL,
    expira_em DATETIMEOFFSET(6) NOT NULL,
    CONSTRAINT pk_api_idempotency PRIMARY KEY (id),
    CONSTRAINT fk_api_idempotency_usuario FOREIGN KEY (usuario_id) REFERENCES dbo.usuarios(id),
    CONSTRAINT uq_api_idempotency_operacao UNIQUE (usuario_id, device_id, operation_id),
    CONSTRAINT ck_api_idempotency_hash CHECK (LEN(request_hash) = 64),
    CONSTRAINT ck_api_idempotency_status CHECK (http_status BETWEEN 100 AND 599),
    CONSTRAINT ck_api_idempotency_datas CHECK (expira_em > criado_em),
    CONSTRAINT ck_api_idempotency_response_json CHECK (ISJSON(response_json) = 1)
);

CREATE INDEX ix_api_idempotency_expiracao
    ON dbo.api_idempotency(expira_em);
