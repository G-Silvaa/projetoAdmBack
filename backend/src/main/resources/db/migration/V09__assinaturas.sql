-- ============================================================================
-- Planos e assinaturas do SaaS.
-- ============================================================================

-- Catálogo de planos (compartilhado entre todas as empresas).
CREATE TABLE liv.planos (
    id BIGSERIAL NOT NULL,
    codigo VARCHAR(30) NOT NULL,
    nome VARCHAR(60) NOT NULL,
    preco NUMERIC(19,2) NOT NULL,
    max_usuarios INTEGER,     -- NULL = ilimitado
    max_clientes INTEGER,     -- NULL = ilimitado
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT planos_pkey PRIMARY KEY (id),
    CONSTRAINT planos_uk_codigo UNIQUE (codigo)
);

-- Planos iniciais (mesmos preços/limites da landing).
INSERT INTO liv.planos (codigo, nome, preco, max_usuarios, max_clientes) VALUES
    ('normal',       'Normal',       80.00,  1,    50),
    ('profissional', 'Profissional', 110.00, 5,    NULL),
    ('escritorio',   'Escritório',   397.00, NULL, NULL);

-- Assinatura: uma por empresa.
CREATE TABLE liv.assinaturas (
    id BIGSERIAL NOT NULL,
    empresa_id BIGINT NOT NULL,
    plano_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    trial_ate DATE,
    inicio DATE NOT NULL DEFAULT CURRENT_DATE,
    vencimento DATE,
    criado_em TIMESTAMP NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT assinaturas_pkey PRIMARY KEY (id),
    CONSTRAINT assinaturas_uk_empresa UNIQUE (empresa_id),
    CONSTRAINT assinaturas_fk_empresa FOREIGN KEY (empresa_id) REFERENCES liv.empresas(id),
    CONSTRAINT assinaturas_fk_plano FOREIGN KEY (plano_id) REFERENCES liv.planos(id)
);

-- A empresa padrão (dados já existentes) recebe uma assinatura ATIVA e sem
-- vencimento próximo, para não bloquear o que já estava em uso.
INSERT INTO liv.assinaturas (empresa_id, plano_id, status, inicio, vencimento)
SELECT 1, p.id, 'ATIVA', CURRENT_DATE, DATE '2099-12-31'
FROM liv.planos p
WHERE p.codigo = 'escritorio';
