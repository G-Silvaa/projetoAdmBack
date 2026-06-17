-- ============================================================================
-- Multi-tenancy (SaaS): cada escritório é uma "empresa" (tenant) e todos os
-- dados de negócio passam a pertencer a uma empresa, ficando isolados por ela.
-- O isolamento em runtime é feito pelo Hibernate via @TenantId (empresa_id).
-- ============================================================================

-- 1) Tabela de empresas (tenants)
CREATE TABLE liv.empresas (
    id BIGSERIAL NOT NULL,
    nome VARCHAR(160) NOT NULL,
    cnpj VARCHAR(18),
    email VARCHAR(160),
    telefone VARCHAR(20),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT empresas_pkey PRIMARY KEY (id)
);

-- Empresa padrão (id = 1) que herda todos os dados já existentes
INSERT INTO liv.empresas (nome, email) VALUES ('Escritório Padrão', NULL);

-- 2) Adiciona empresa_id em todas as tabelas multi-tenant, faz backfill para a
--    empresa 1, torna NOT NULL e cria a foreign key.

-- usuarios
ALTER TABLE liv.usuarios ADD COLUMN empresa_id BIGINT;
UPDATE liv.usuarios SET empresa_id = 1;
ALTER TABLE liv.usuarios ALTER COLUMN empresa_id SET NOT NULL;
ALTER TABLE liv.usuarios ADD CONSTRAINT usuarios_fk_empresa
    FOREIGN KEY (empresa_id) REFERENCES liv.empresas(id);

-- representantes
ALTER TABLE liv.representantes ADD COLUMN empresa_id BIGINT;
UPDATE liv.representantes SET empresa_id = 1;
ALTER TABLE liv.representantes ALTER COLUMN empresa_id SET NOT NULL;
ALTER TABLE liv.representantes ADD CONSTRAINT representantes_fk_empresa
    FOREIGN KEY (empresa_id) REFERENCES liv.empresas(id);

-- clientes
ALTER TABLE liv.clientes ADD COLUMN empresa_id BIGINT;
UPDATE liv.clientes SET empresa_id = 1;
ALTER TABLE liv.clientes ALTER COLUMN empresa_id SET NOT NULL;
ALTER TABLE liv.clientes ADD CONSTRAINT clientes_fk_empresa
    FOREIGN KEY (empresa_id) REFERENCES liv.empresas(id);

-- contratos
ALTER TABLE liv.contratos ADD COLUMN empresa_id BIGINT;
UPDATE liv.contratos SET empresa_id = 1;
ALTER TABLE liv.contratos ALTER COLUMN empresa_id SET NOT NULL;
ALTER TABLE liv.contratos ADD CONSTRAINT contratos_fk_empresa
    FOREIGN KEY (empresa_id) REFERENCES liv.empresas(id);

-- processos
ALTER TABLE liv.processos ADD COLUMN empresa_id BIGINT;
UPDATE liv.processos SET empresa_id = 1;
ALTER TABLE liv.processos ALTER COLUMN empresa_id SET NOT NULL;
ALTER TABLE liv.processos ADD CONSTRAINT processos_fk_empresa
    FOREIGN KEY (empresa_id) REFERENCES liv.empresas(id);

-- financeiros
ALTER TABLE liv.financeiros ADD COLUMN empresa_id BIGINT;
UPDATE liv.financeiros SET empresa_id = 1;
ALTER TABLE liv.financeiros ALTER COLUMN empresa_id SET NOT NULL;
ALTER TABLE liv.financeiros ADD CONSTRAINT financeiros_fk_empresa
    FOREIGN KEY (empresa_id) REFERENCES liv.empresas(id);

-- 3) Unicidade agora é POR EMPRESA (dois escritórios podem ter o mesmo CPF /
--    número de contrato / protocolo). O e-mail de usuário continua global,
--    pois o login localiza o usuário por e-mail antes de conhecer o tenant.
ALTER TABLE liv.clientes DROP CONSTRAINT IF EXISTS clientes_uk_cpf;
ALTER TABLE liv.clientes ADD CONSTRAINT clientes_uk_empresa_cpf UNIQUE (empresa_id, cpf);

ALTER TABLE liv.contratos DROP CONSTRAINT IF EXISTS contratos_uk_numero;
ALTER TABLE liv.contratos ADD CONSTRAINT contratos_uk_empresa_numero UNIQUE (empresa_id, numero);

ALTER TABLE liv.processos DROP CONSTRAINT IF EXISTS processos_uk_numero_protocolo;
ALTER TABLE liv.processos ADD CONSTRAINT processos_uk_empresa_protocolo UNIQUE (empresa_id, numero_protocolo);

-- 4) Recria a view de relatórios particionando por empresa (o @TenantId na
--    entidade Relatorio passa a filtrar os agregados pela empresa atual).
DROP VIEW IF EXISTS liv.vw_relatorios;
CREATE VIEW liv.vw_relatorios AS
WITH anos_mes AS (
    SELECT p.empresa_id AS empresa_id,
           EXTRACT(YEAR FROM p.data_criacao) AS ano,
           EXTRACT(MONTH FROM p.data_criacao) AS mes
    FROM liv.processos p
    WHERE p.data_criacao IS NOT NULL
    GROUP BY p.empresa_id, EXTRACT(YEAR FROM p.data_criacao), EXTRACT(MONTH FROM p.data_criacao)
    UNION
    SELECT p.empresa_id,
           EXTRACT(YEAR FROM p.entrada_do_protocolo),
           EXTRACT(MONTH FROM p.entrada_do_protocolo)
    FROM liv.processos p
    WHERE p.entrada_do_protocolo IS NOT NULL
    GROUP BY p.empresa_id, EXTRACT(YEAR FROM p.entrada_do_protocolo), EXTRACT(MONTH FROM p.entrada_do_protocolo)
    UNION
    SELECT p.empresa_id,
           EXTRACT(YEAR FROM p.data_concessao),
           EXTRACT(MONTH FROM p.data_concessao)
    FROM liv.processos p
    WHERE p.data_concessao IS NOT NULL AND p.status = 'APROVADO'
    GROUP BY p.empresa_id, EXTRACT(YEAR FROM p.data_concessao), EXTRACT(MONTH FROM p.data_concessao)
),
dados_processos AS (
    SELECT p.empresa_id AS empresa_id,
           EXTRACT(YEAR FROM p.data_criacao) AS ano,
           EXTRACT(MONTH FROM p.data_criacao) AS mes,
           COUNT(p.id) AS total_contratos
    FROM liv.processos p
    WHERE p.data_criacao IS NOT NULL
    GROUP BY p.empresa_id, EXTRACT(YEAR FROM p.data_criacao), EXTRACT(MONTH FROM p.data_criacao)
),
dado_entrada AS (
    SELECT p.empresa_id AS empresa_id,
           EXTRACT(YEAR FROM p.entrada_do_protocolo) AS ano,
           EXTRACT(MONTH FROM p.entrada_do_protocolo) AS mes,
           COUNT(*) AS dado_entrada
    FROM liv.processos p
    WHERE p.entrada_do_protocolo IS NOT NULL
    GROUP BY p.empresa_id, EXTRACT(YEAR FROM p.entrada_do_protocolo), EXTRACT(MONTH FROM p.entrada_do_protocolo)
),
beneficios_concedidos AS (
    SELECT p.empresa_id AS empresa_id,
           EXTRACT(YEAR FROM p.data_concessao) AS ano,
           EXTRACT(MONTH FROM p.data_concessao) AS mes,
           COUNT(*) AS total_concedidos
    FROM liv.processos p
    WHERE p.data_concessao IS NOT NULL AND p.status = 'APROVADO'
    GROUP BY p.empresa_id, EXTRACT(YEAR FROM p.data_concessao), EXTRACT(MONTH FROM p.data_concessao)
)
SELECT
    (a.ano * 100 + a.mes) AS id,
    a.empresa_id AS empresa_id,
    a.ano,
    a.mes,
    COALESCE(dp.total_contratos, 0) AS total_contratos,
    COALESCE(dc.total_concedidos, 0) AS total_beneficios_concedidos,
    COALESCE(de.dado_entrada, 0) AS dado_entrada,
    COUNT(CASE WHEN p.status <> 'APROVADO'
               AND p.status <> 'REPROVADO'
               AND p.status <> 'PENDENTE'
               AND p.status <> 'AGUARDANDO'
               THEN 1 END) AS total_beneficios_aguardando
FROM anos_mes a
LEFT JOIN dados_processos dp
    ON dp.empresa_id = a.empresa_id AND dp.ano = a.ano AND dp.mes = a.mes
LEFT JOIN dado_entrada de
    ON de.empresa_id = a.empresa_id AND de.ano = a.ano AND de.mes = a.mes
LEFT JOIN beneficios_concedidos dc
    ON dc.empresa_id = a.empresa_id AND dc.ano = a.ano AND dc.mes = a.mes
LEFT JOIN liv.processos p
    ON p.empresa_id = a.empresa_id
    AND EXTRACT(YEAR FROM p.data_criacao) = a.ano
    AND EXTRACT(MONTH FROM p.data_criacao) = a.mes
GROUP BY a.empresa_id, a.ano, a.mes, de.dado_entrada, dc.total_concedidos, dp.total_contratos
ORDER BY a.empresa_id, a.ano, a.mes;
