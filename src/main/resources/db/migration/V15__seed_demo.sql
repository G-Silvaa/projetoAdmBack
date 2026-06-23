-- ============================================================================
-- Seed de demonstração do escritório padrão (empresa_id = 1).
--
-- Objetivo: quem clonar o projeto sobe o sistema já populado (clientes,
-- contratos, processos, financeiro) e com o dashboard "aceso".
--
-- Idempotente e seguro:
--   * Datas relativas a CURRENT_DATE (sempre "recentes" para quem clona).
--   * IDs por sequence (BIGSERIAL) + FKs resolvidas por chave natural
--     (cpf / número de contrato) — nunca colidem com dados existentes.
--   * O bloco de dados só roda quando a empresa 1 ainda não tem clientes,
--     então NÃO duplica nada em bases que já estão em uso.
-- ============================================================================

-- 1) Assinatura da empresa demo: deixa ATIVA com vencimento crível (1 ano a
--    partir de agora), em vez do antigo sentinela 2099/2100. Sempre aplicado.
UPDATE liv.assinaturas
SET status        = 'ATIVA',
    inicio        = CURRENT_DATE,
    trial_ate     = NULL,
    vencimento    = (CURRENT_DATE + INTERVAL '1 year')::date,
    atualizado_em = now()
WHERE empresa_id = 1;

-- 2) Dados de negócio — só quando a empresa 1 ainda está "vazia".
DO $$
BEGIN
IF NOT EXISTS (SELECT 1 FROM liv.clientes WHERE empresa_id = 1) THEN

    -- Representantes legais
    INSERT INTO liv.representantes (empresa_id, nome, email, telefone, parentesco, cpf, rg, nascimento) VALUES
        (1, 'Cláudia Pereira Costa', 'claudia.costa@email.com', '85991110001', 'Mãe',   '70011122230', '2001122230', DATE '1968-03-12'),
        (1, 'Marcos Antônio Lima',   'marcos.lima@email.com',   '85991110002', 'Filho', '70011122231', '2001122231', DATE '1985-07-22');

    -- Clientes (segurados)
    INSERT INTO liv.clientes
        (empresa_id, nome, email, telefone, cpf, rg, nascimento, endereco_cep, endereco_logradouro, endereco_complemento, endereco_bairro, endereco_cidade, representante_id)
    VALUES
        (1, 'Maria das Graças Silva',     'maria.gracas@email.com',   '85998765432', '53076529501', '2005305297', DATE '1958-04-10', '60440000', 'Rua das Flores, 120',     'Casa',     'Bom Jardim',   'Fortaleza',  (SELECT id FROM liv.representantes WHERE empresa_id=1 AND cpf='70011122230')),
        (1, 'José Carlos Oliveira',       'jose.carlos@email.com',    '85987654321', '14293526129', '2001429352', DATE '1959-11-02', '60360000', 'Av. Bezerra de Menezes, 800', 'Apto 302', 'São Gerardo',  'Fortaleza',  NULL),
        (1, 'Antônia Ferreira Souza',     'antonia.souza@email.com',  '85976543210', '47209308750', '2004720930', DATE '1990-01-15', '60533000', 'Rua Padre Cícero, 45',    NULL,       'Conjunto Ceará','Fortaleza', (SELECT id FROM liv.representantes WHERE empresa_id=1 AND cpf='70011122231')),
        (1, 'Francisco das Chagas Pereira','francisco.chagas@email.com','85954321098', '49554389540', '2004955438', DATE '1980-06-20', '60150000', 'Rua Tibúrcio Cavalcante, 210', 'Casa B', 'Aldeota',     'Fortaleza',  NULL),
        (1, 'Raimunda Nonata Alves',      'raimunda.alves@email.com', '85932109876', '30482157904', '2003048215', DATE '1972-09-05', '60175000', 'Av. dos Expedicionários, 1500', NULL,    'Montese',     'Fortaleza',  NULL),
        (1, 'João Batista Rocha',         'joao.rocha@email.com',     '85998761234', '88512347600', '2008851234', DATE '1962-02-28', '60125000', 'Rua Carlos Vasconcelos, 90', 'Apto 11', 'Meireles',    'Fortaleza',  NULL),
        (1, 'Sebastiana Maria Lima',      'sebastiana.lima@email.com','85991234567', '26719834500', '2002671983', DATE '1995-12-12', '60711000', 'Av. I, 300',              'Casa',     'Jereissati',  'Maracanaú',  NULL),
        (1, 'Pedro Henrique Gomes',       'pedro.gomes@email.com',    '85993456789', '61237894501', '2006123789', DATE '1955-08-19', '60355000', 'Rua Ana Bilhar, 75',      NULL,       'Varjota',     'Fortaleza',  NULL);

    -- Contratos (1 por cliente, cobrindo todas as modalidades; 2 encerrados)
    INSERT INTO liv.contratos (empresa_id, cliente_id, beneficio, valor, indicacao, numero, inicio, conclusao) VALUES
        (1, (SELECT id FROM liv.clientes WHERE empresa_id=1 AND cpf='53076529501'), 'BPC_LOAS__IDOSO',                  4500.00, NULL,                       '11000000001', CURRENT_DATE - 90,  NULL),
        (1, (SELECT id FROM liv.clientes WHERE empresa_id=1 AND cpf='14293526129'), 'APOSENTADORIA_IDADE',              6200.00, 'Indicação: Dra. Helena',   '11000000002', CURRENT_DATE - 120, CURRENT_DATE - 5),
        (1, (SELECT id FROM liv.clientes WHERE empresa_id=1 AND cpf='47209308750'), 'BPC_LOAS__DEFICIENTE',             5000.00, NULL,                       '11000000003', CURRENT_DATE - 60,  NULL),
        (1, (SELECT id FROM liv.clientes WHERE empresa_id=1 AND cpf='49554389540'), 'AUXILIO_INCAPACIDADE_TEMPORARIA',  4800.00, 'Indicação de cliente',     '11000000004', CURRENT_DATE - 45,  NULL),
        (1, (SELECT id FROM liv.clientes WHERE empresa_id=1 AND cpf='30482157904'), 'PENSAO_MORTE',                     5500.00, NULL,                       '11000000005', CURRENT_DATE - 75,  NULL),
        (1, (SELECT id FROM liv.clientes WHERE empresa_id=1 AND cpf='88512347600'), 'APOSENTADORIA_TEMPO_CONTRIBUICAO', 7000.00, 'Indicação: parceria contábil', '11000000006', CURRENT_DATE - 150, CURRENT_DATE - 8),
        (1, (SELECT id FROM liv.clientes WHERE empresa_id=1 AND cpf='26719834500'), 'SALARIO_MATERNIDADE',              2400.00, NULL,                       '11000000007', CURRENT_DATE - 30,  NULL),
        (1, (SELECT id FROM liv.clientes WHERE empresa_id=1 AND cpf='61237894501'), 'APOSENTADORIA_INVALIDEZ',          5300.00, NULL,                       '11000000008', CURRENT_DATE - 100, NULL);

    -- Processos (1 por contrato, status variados; datas espalhadas em vários meses)
    INSERT INTO liv.processos
        (empresa_id, contrato_id, status, numero_protocolo, documentos_pendentes, entrada_do_protocolo, data_criacao, ultima_atualizacao, pericia_medica, endereco_pericia_medica, data_concessao, valor_concedido)
    VALUES
        (1, (SELECT id FROM liv.contratos WHERE empresa_id=1 AND numero='11000000001'), 'ANALISE',                 '21000000001', NULL,                                        CURRENT_DATE - 85,  CURRENT_DATE - 90,  now(), NULL, NULL, NULL, NULL),
        (1, (SELECT id FROM liv.contratos WHERE empresa_id=1 AND numero='11000000002'), 'APROVADO',                '21000000002', NULL,                                        CURRENT_DATE - 115, CURRENT_DATE - 120, now(), NULL, NULL, CURRENT_DATE - 20, 6200.00),
        (1, (SELECT id FROM liv.contratos WHERE empresa_id=1 AND numero='11000000003'), 'ANALISE_ADMINISTRATIVA',  '21000000003', NULL,                                        CURRENT_DATE - 55,  CURRENT_DATE - 60,  now(), now() + INTERVAL '10 days', 'INSS Agência Fortaleza Centro - Rua Major Facundo, 200', NULL, NULL),
        (1, (SELECT id FROM liv.contratos WHERE empresa_id=1 AND numero='11000000004'), 'PENDENTE',                NULL,          'RG, comprovante de residência e laudo médico', CURRENT_DATE - 40,  CURRENT_DATE - 45,  now(), NULL, NULL, NULL, NULL),
        (1, (SELECT id FROM liv.contratos WHERE empresa_id=1 AND numero='11000000005'), 'APROVADO',                '21000000005', NULL,                                        CURRENT_DATE - 70,  CURRENT_DATE - 75,  now(), NULL, NULL, CURRENT_DATE - 10, 5500.00),
        (1, (SELECT id FROM liv.contratos WHERE empresa_id=1 AND numero='11000000006'), 'CUMPRIMENTO_EXIGENCIA',   '21000000006', 'Cumprir exigência: anexar laudo pericial',  CURRENT_DATE - 140, CURRENT_DATE - 150, now(), NULL, NULL, NULL, NULL),
        (1, (SELECT id FROM liv.contratos WHERE empresa_id=1 AND numero='11000000007'), 'AGUARDANDO',              NULL,          NULL,                                        NULL,               CURRENT_DATE - 30,  now(), NULL, NULL, NULL, NULL),
        (1, (SELECT id FROM liv.contratos WHERE empresa_id=1 AND numero='11000000008'), 'ANALISE',                 '21000000008', NULL,                                        CURRENT_DATE - 95,  CURRENT_DATE - 100, now(), NULL, NULL, NULL, NULL);

    -- Financeiro (parcelamentos: 4 quitados/em dia e 1 em aberto)
    INSERT INTO liv.financeiros
        (empresa_id, contrato_id, parcelas_restantes, parcelas_pagas, valor_proxima_parcela, montante_pago, numero_solicitacao_pagamento, data_pagamento_parcela, valor_total_pagar, valor_pago_da_parcela, situacao_parcela, situacao_pagamento)
    VALUES
        (1, (SELECT id FROM liv.contratos WHERE empresa_id=1 AND numero='11000000001'),  8,  4, 562.50, 2250.00, NULL, CURRENT_DATE - 10, 4500.00, 562.50, 'Pago',                  TRUE),
        (1, (SELECT id FROM liv.contratos WHERE empresa_id=1 AND numero='11000000002'),  0, 12,   0.00, 6200.00, NULL, CURRENT_DATE - 5,  6200.00, 516.67, 'Quitado',               TRUE),
        (1, (SELECT id FROM liv.contratos WHERE empresa_id=1 AND numero='11000000004'), 10,  2, 400.00,  800.00, NULL, NULL,              4800.00, 400.00, 'Aguardando pagamento',  FALSE),
        (1, (SELECT id FROM liv.contratos WHERE empresa_id=1 AND numero='11000000005'),  6,  6, 458.33, 2750.00, NULL, CURRENT_DATE - 10, 5500.00, 458.33, 'Pago',                  TRUE),
        (1, (SELECT id FROM liv.contratos WHERE empresa_id=1 AND numero='11000000006'),  0, 12,   0.00, 7000.00, NULL, CURRENT_DATE - 8,  7000.00, 583.33, 'Quitado',               TRUE);

END IF;
END $$;
