-- Dados do escritório usados na geração de contratos (configuráveis por empresa).
ALTER TABLE liv.empresas ADD COLUMN endereco VARCHAR(200);
ALTER TABLE liv.empresas ADD COLUMN cidade VARCHAR(80);
ALTER TABLE liv.empresas ADD COLUMN pix_chave VARCHAR(140);
ALTER TABLE liv.empresas ADD COLUMN dados_bancarios VARCHAR(200);

-- Preenche a empresa padrão (id 1) com os dados originais do escritório,
-- para que os contratos já existentes continuem com as informações corretas.
UPDATE liv.empresas SET
    nome = 'LIV Assessoria Previdenciária LTDA',
    cnpj = '48.994.154/0001-72',
    telefone = '(85) 9 8628-5349',
    email = 'liv.assessoria.previdenciaria@outlook.com',
    endereco = 'Av. 4 de julho, nº 387, Jereissati II',
    cidade = 'Maracanaú/CE',
    pix_chave = '(85) 9 9958-2811 · Banco Itaú',
    dados_bancarios = 'Itaú Unibanco (341) · Agência 7979 · Conta 54046-0'
WHERE id = 1;
