-- Renomeia o escritório padrão de "LIV" para "Arctech".
-- O nome da empresa alimenta a geração do PDF do contrato (Contrato.gerarContrato).
-- Mantém CNPJ, endereço, telefone, banco e e-mail inalterados (dados reais do escritório).
UPDATE liv.empresas SET
    nome = 'Arctech Assessoria Previdenciária'
WHERE id = 1
  AND nome = 'LIV Assessoria Previdenciária LTDA';
