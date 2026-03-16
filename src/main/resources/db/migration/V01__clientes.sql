CREATE TABLE liv.representantes(
    id BIGSERIAL NOT NULL,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(60),
    telefone VARCHAR(11),
    parentesco VARCHAR(100) NOT NULL,
    cpf VARCHAR(15),
    rg VARCHAR(20),
    nascimento DATE,
    CONSTRAINT representantes_pkey PRIMARY KEY(id)
);

CREATE TABLE liv.clientes (
    id BIGSERIAL NOT NULL,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(60),
    telefone VARCHAR(11),
    cpf VARCHAR(15),
    rg VARCHAR(20),
    nascimento DATE,
    endereco_cep VARCHAR(8),
    endereco_logradouro VARCHAR(100),
    endereco_complemento VARCHAR(20),
    endereco_bairro VARCHAR(30),
    endereco_cidade VARCHAR(50),
    representante_id int8,
    CONSTRAINT clientes_pkey PRIMARY KEY(id),
    CONSTRAINT cliente_fk_representante FOREIGN KEY(representante_id) REFERENCES liv.representantes(id)
);