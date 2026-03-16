ALTER TABLE liv.processos ADD COLUMN numero_protocolo VARCHAR(15);

ALTER TABLE liv.processos ADD CONSTRAINT processos_uk_numero_protocolo UNIQUE (numero_protocolo);

ALTER TABLE liv.clientes ADD CONSTRAINT clientes_uk_cpf UNIQUE (cpf);

ALTER TABLE liv.processos ADD COLUMN data_concessao DATE;

ALTER TABLE liv.processos ADD COLUMN cessacao DATE;

ALTER TABLE liv.processos ADD COLUMN data_criacao DATE;

ALTER TABLE liv.processos ADD COLUMN endereco_pericia_medica VARCHAR(255);

ALTER TABLE liv.processos ADD COLUMN endereco_avaliacao_social VARCHAR(255);

ALTER TABLE liv.contratos DROP CONSTRAINT contrato_fk_beneficio;

ALTER TABLE liv.contratos DROP COLUMN beneficio_id;

DROP TABLE liv.beneficios;

ALTER TABLE liv.contratos ADD COLUMN beneficio VARCHAR(100);

CREATE OR REPLACE VIEW liv.vw_relatorios AS
WITH anos_mes AS (
    -- Cria uma tabela de referência com todos os anos e meses possíveis
    SELECT
        EXTRACT(YEAR FROM p.data_criacao) AS ano,
        EXTRACT(MONTH FROM p.data_criacao) AS mes
    FROM liv.processos p
    GROUP BY ano, mes
    UNION
    -- Também inclui os anos e meses das entradas de protocolo
    SELECT
        EXTRACT(YEAR FROM entrada_do_protocolo) AS ano,
        EXTRACT(MONTH FROM entrada_do_protocolo) AS mes
    FROM liv.processos
    WHERE entrada_do_protocolo IS NOT NULL
    GROUP BY ano, mes
    UNION
    -- E inclui os anos e meses das concessões de benefícios
    SELECT
        EXTRACT(YEAR FROM data_concessao) AS ano,
        EXTRACT(MONTH FROM data_concessao) AS mes
    FROM liv.processos
    WHERE data_concessao IS NOT NULL AND status = 'APROVADO'
    GROUP BY ano, mes
),
dados_processos AS (
    -- Contagem de processos agrupados por ano/mês
    SELECT
        EXTRACT(YEAR FROM p.data_criacao) AS ano,
        EXTRACT(MONTH FROM p.data_criacao) AS mes,
        COUNT(p.id) AS total_contratos
    FROM liv.processos p
    GROUP BY ano, mes
),
dado_entrada AS (
    -- Contagem de entradas do protocolo agrupadas por ano/mês
    SELECT
        EXTRACT(YEAR FROM entrada_do_protocolo) AS ano,
        EXTRACT(MONTH FROM entrada_do_protocolo) AS mes,
        COUNT(*) AS dado_entrada
    FROM liv.processos
    WHERE entrada_do_protocolo IS NOT NULL
    GROUP BY ano, mes
),
beneficios_concedidos AS (
    -- Contagem de benefícios concedidos agrupados por ano/mês
    SELECT
        EXTRACT(YEAR FROM data_concessao) AS ano,
        EXTRACT(MONTH FROM data_concessao) AS mes,
        COUNT(*) AS total_concedidos
    FROM liv.processos
    WHERE data_concessao IS NOT NULL AND status = 'APROVADO'
    GROUP BY ano, mes
)

SELECT
    (a.ano * 100 + a.mes) AS id,
    a.ano,
    a.mes,
    COALESCE(dp.total_contratos, 0) AS total_contratos,
    COALESCE(dc.total_concedidos, 0) AS total_beneficios_concedidos,
    COALESCE(de.dado_entrada, 0) AS dado_entrada,  -- Alias dado_entrada without renaming
    COUNT(CASE WHEN p.status <> 'APROVADO' 
               AND p.status <> 'REPROVADO' 
               AND p.status <> 'PENDENTE' 
               AND p.status <> 'AGUARDANDO' 
               THEN 1 END) AS total_beneficios_aguardando
FROM anos_mes a
LEFT JOIN dados_processos dp 
    ON dp.ano = a.ano AND dp.mes = a.mes
LEFT JOIN dado_entrada de
    ON de.ano = a.ano AND de.mes = a.mes
LEFT JOIN beneficios_concedidos dc
    ON dc.ano = a.ano AND dc.mes = a.mes
LEFT JOIN liv.processos p
    ON EXTRACT(YEAR FROM p.data_criacao) = a.ano
    AND EXTRACT(MONTH FROM p.data_criacao) = a.mes
GROUP BY a.ano, a.mes, de.dado_entrada, dc.total_concedidos, dp.total_contratos
ORDER BY a.ano, a.mes;
