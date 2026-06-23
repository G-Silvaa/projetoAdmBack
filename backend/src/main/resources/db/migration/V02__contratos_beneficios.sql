CREATE TABLE liv.beneficios(
    id BIGSERIAL NOT NULL,
    nome VARCHAR(100) NOT NULL,
    especie VARCHAR(3) NOT NULL,
    CONSTRAINT beneficios_pkey PRIMARY KEY(id),
    CONSTRAINT beneficios_uk_especie UNIQUE(especie)
);

INSERT INTO liv.beneficios(id, nome, especie) VALUES(1, 'BPC/LOAS ao Deficiente', '87');
INSERT INTO liv.beneficios(id, nome, especie) VALUES(2, 'BPC/LOAS ao Idoso', '88');
INSERT INTO liv.beneficios(id, nome, especie) VALUES(3, 'Aposentadoria por Idade', '41');
INSERT INTO liv.beneficios(id, nome, especie) VALUES(4, 'Aposentadoria por Tempo de Contribuição', '42');
INSERT INTO liv.beneficios(id, nome, especie) VALUES(5, 'Aposentadoria por Invalidez', '32');
INSERT INTO liv.beneficios(id, nome, especie) VALUES(6, 'Pensão por Morte', '21');
INSERT INTO liv.beneficios(id, nome, especie) VALUES(7, 'Auxílio Reclusão', '25');
INSERT INTO liv.beneficios(id, nome, especie) VALUES(8, 'Auxílio por Incapacidade Temporária', '31');
INSERT INTO liv.beneficios(id, nome, especie) VALUES(9, 'Auxílio Acidente', '36');
INSERT INTO liv.beneficios(id, nome, especie) VALUES(10, 'Salário Maternidade', '80');

CREATE TABLE liv.contratos(
    id BIGSERIAL NOT NULL,
    cliente_id int8 NOT NULL,
    beneficio_id int8 NOT NULL,
    inicio DATE DEFAULT CURRENT_DATE,
    conclusao DATE,
    CONSTRAINT contratos_pkey PRIMARY KEY(id),
    CONSTRAINT contratos_fk_cliente FOREIGN KEY(cliente_id) REFERENCES liv.clientes(id),
    CONSTRAINT contrato_fk_beneficio FOREIGN KEY(beneficio_id) REFERENCES liv.beneficios(id)
);