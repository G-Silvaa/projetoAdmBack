INSERT INTO liv.representantes (nome, email, telefone, parentesco, cpf, rg, nascimento) VALUES
('Lucas Martins', 'lucas.martins@example.com', '33344455566', 'Irmão', '12365478900', '981234567', '1992-03-12'),
('Fernanda Souza', 'fernanda.souza@example.com', '55566677788', 'Irmã', '98732165400', '765432101', '1988-07-30'),
('Ricardo Almeida', 'ricardo.almeida@example.com', '22233344455', 'Cônjuge', '45678912300', '345678901', '1979-11-05');

SELECT * FROM liv.representantes;

INSERT INTO liv.clientes (nome, email, telefone, cpf, rg, nascimento, endereco_cep, endereco_logradouro, endereco_complemento, endereco_bairro, endereco_cidade, representante_id) VALUES
('Juliana Rocha', 'juliana.rocha@example.com', '11223344557', '32165498701', '876543210', '1995-01-22', '12345678', 'Avenida Central', 'Casa 2', 'Centro', 'Fortaleza', 1),
('Marcos Pereira', 'marcos.pereira@example.com', '22334455678', '45678912301', '765432101', '1983-05-13', '87654321', 'Rua das Flores', '', 'Jardim', 'Caucaia', 2),
('Patrícia Lima', 'patricia.lima@example.com', '33445566789', '23456789001', '123456789', '1991-08-17', '23456789', 'Rua do Comércio', 'Apto 303', 'Centro', 'Maracanaú', 3),
('Fernando Alves', 'fernando.alves@example.com', '44556677890', '56789012301', '987654321', '1980-02-14', '34567890', 'Avenida dos Trabalhadores', '', 'Bairro Novo', 'Caucaia', null),
('Ana Clara', 'anaclara@example.com', '55667788901', '78901234501', '219876540', '1994-11-30', '45678901', 'Rua dos Lírios', 'Casa 1', 'Bairro Jardim', 'Fortaleza', null);

INSERT INTO liv.clientes (nome, email, telefone, cpf, rg, nascimento, endereco_cep, endereco_logradouro, endereco_complemento, endereco_bairro, endereco_cidade, representante_id) VALUES
('Antonio Jubileu', 'antoniojubileu@example.com', '85981704425', '86536007018', '463460285', '2003-11-30', '61809255', 'Rua dos Bezerra', 'Casa 14', 'Pavuna', 'Pacatuba', null);

INSERT INTO liv.clientes (nome, email, telefone, cpf, rg, nascimento, endereco_cep, endereco_logradouro, endereco_complemento, endereco_bairro, endereco_cidade, representante_id) VALUES
('Caetano Meloso', 'caetanomeloso@example.com', '8599582811', '53636164082', '225043178', '1990-11-30', '61809255', 'Rua dos Bezerra', 'Casa 10', 'Pavuna', 'Pacatuba', null);

SELECT * FROM liv.clientes;

-- Contratos para Juliana Rocha
INSERT INTO liv.contratos (cliente_id, beneficio, inicio, conclusao) VALUES
(1, 'APOSENTADORIA_INVALIDEZ', '2024-10-10', NULL);  -- Aposentadoria por invalidez

SELECT * FROM liv.contratos;

-- Criando processos manualmente para cada contrato inserido
INSERT INTO liv.processos (status, contrato_id, ultima_atualizacao, data_criacao) VALUES
('PENDENTE', 1, CURRENT_TIMESTAMP, '2024-10-10');

SELECT * FROM liv.processos;

-- Contratos para Marcos Pereira
INSERT INTO liv.contratos (cliente_id, beneficio, inicio, conclusao) VALUES
(2, 'BPC_LOAS__IDOSO', '2024-10-15', NULL);  -- BPC/LOAS ao Idoso

-- Criando processos manualmente para cada contrato inserido
INSERT INTO liv.processos (status, contrato_id, ultima_atualizacao, data_criacao) VALUES
('AGUARDANDO', 2, CURRENT_TIMESTAMP, '2024-10-15');

-- Contratos para Patrícia Lima
INSERT INTO liv.contratos (cliente_id, beneficio, inicio, conclusao) VALUES
(3, 'AUXILIO_ACIDENTE', '2024-09-30', NULL);  -- Auxílio acidente

-- Criando processos manualmente para cada contrato inserido
INSERT INTO liv.processos (status, contrato_id, numero_protocolo, entrada_do_protocolo, ultima_atualizacao, data_criacao) VALUES
('ANALISE', 3, '398489', '2024-10-11', CURRENT_TIMESTAMP, '2024-09-30');

-- Contratos para Fernando Alves
INSERT INTO liv.contratos (cliente_id, beneficio, inicio, conclusao) VALUES
(4, 'BPC_LOAS__DEFICIENTE', '2024-11-05', NULL);  -- BPC/LOAS ao Deficiente

-- Criando processos manualmente para cada contrato inserido
INSERT INTO liv.processos (status, contrato_id, numero_protocolo, entrada_do_protocolo, ultima_atualizacao, data_criacao) VALUES
('CUMPRIMENTO_EXIGENCIA', 4, '007827', '2024-11-10', CURRENT_TIMESTAMP, '2024-11-05');

-- Contratos para Ana Clara
INSERT INTO liv.contratos (cliente_id, beneficio, inicio, conclusao) VALUES
(5, 'BPC_LOAS__DEFICIENTE', '2024-10-15', NULL);  -- Auxílio reclusão

-- Criando processos manualmente para cada contrato inserido
INSERT INTO liv.processos (status, contrato_id, numero_protocolo, entrada_do_protocolo, pericia_medica, ultima_atualizacao, data_criacao, endereco_pericia_medica) VALUES
('ANALISE_ADMINISTRATIVA', 5, '872673', '2024-10-24', '2024-12-20', CURRENT_TIMESTAMP, '2024-10-15', 'AGÊNCIA DA PREVIDÊNCIA SOCIAL FORTALEZA - AV DOUTOR VALMIR PONTES, S/N, EDSON QUEIROZ');

-- Contratos para Antonio Jubileu
INSERT INTO liv.contratos (cliente_id, beneficio, inicio, conclusao) VALUES
(6, 'BPC_LOAS__DEFICIENTE', '2024-10-17', NULL);  -- Auxílio reclusão

-- Criando processos manualmente para cada contrato inserido
INSERT INTO liv.processos (status, contrato_id, numero_protocolo, entrada_do_protocolo, pericia_medica, avaliacao_social, ultima_atualizacao, data_criacao, endereco_avaliacao_social, endereco_pericia_medica) VALUES
('ANALISE_ADMINISTRATIVA', 6, '0826271', '2024-10-26', '2024-12-17T10:00', '2025-01-5T16:20', CURRENT_TIMESTAMP, '2024-10-17', 'AGÊNCIA DA PREVIDÊNCIA SOCIAL FORTALEZA - AV DOUTOR VALMIR PONTES, S/N, EDSON QUEIROZ', 'AGÊNCIA DA PREVIDÊNCIA SOCIAL FORTALEZA - AV DOUTOR VALMIR PONTES, S/N, EDSON QUEIROZ');

-- Contratos para Caetano Meloso
INSERT INTO liv.contratos (cliente_id, beneficio, inicio, conclusao) VALUES
(7, 'BPC_LOAS__IDOSO', '2024-09-01', NULL);  -- Auxílio reclusão

-- Criando processos manualmente para cada contrato inserido
INSERT INTO liv.processos (status, contrato_id, numero_protocolo, entrada_do_protocolo, ultima_atualizacao, data_criacao, data_concessao) VALUES
('APROVADO', 7, '0083721', '2024-09-10', CURRENT_TIMESTAMP, '2024-09-01', '2024-10-01');
