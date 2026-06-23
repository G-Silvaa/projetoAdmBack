-- ============================================================================
-- Cobranças PIX das assinaturas (integração AbacatePay / modo sandbox).
-- Cada cobrança representa uma tentativa de pagamento de uma assinatura.
-- ============================================================================

CREATE TABLE liv.cobrancas (
    id BIGSERIAL NOT NULL,
    empresa_id BIGINT NOT NULL,
    plano_id BIGINT NOT NULL,
    provider_id VARCHAR(120),              -- id da cobrança no provedor (AbacatePay)
    valor NUMERIC(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL,           -- PENDENTE, PAGO, EXPIRADO, CANCELADO
    br_code TEXT,                          -- PIX copia e cola (EMV)
    br_code_base64 TEXT,                   -- imagem do QR code (data URI)
    expira_em TIMESTAMP,
    pago_em TIMESTAMP,
    criado_em TIMESTAMP NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT cobrancas_pkey PRIMARY KEY (id),
    CONSTRAINT cobrancas_fk_empresa FOREIGN KEY (empresa_id) REFERENCES liv.empresas(id),
    CONSTRAINT cobrancas_fk_plano FOREIGN KEY (plano_id) REFERENCES liv.planos(id)
);

CREATE INDEX cobrancas_idx_empresa ON liv.cobrancas (empresa_id);
CREATE INDEX cobrancas_idx_provider ON liv.cobrancas (provider_id);
