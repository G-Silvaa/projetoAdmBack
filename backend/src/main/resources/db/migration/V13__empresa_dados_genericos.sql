-- Substitui os dados de contato/legais remanescentes da LIV por placeholders genéricos da Arctech.
-- Trocar pelos dados reais do escritório quando disponíveis.
UPDATE liv.empresas SET
    cnpj = '00.000.000/0001-00',
    telefone = '(00) 0000-0000',
    email = 'contato@arctech.com.br',
    endereco = 'Rua Exemplo, nº 100, Centro',
    cidade = 'Cidade/UF',
    pix_chave = 'contato@arctech.com.br',
    dados_bancarios = 'Banco 000 · Agência 0000 · Conta 00000-0'
WHERE id = 1;
