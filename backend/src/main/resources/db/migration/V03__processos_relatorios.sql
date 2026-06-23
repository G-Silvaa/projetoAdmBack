CREATE TABLE liv.processos(
    id BIGSERIAL NOT NULL,
    status VARCHAR(30) NOT NULL,
    documentos_pendentes VARCHAR(255),
    contrato_id int8 NOT NULL,
    pericia_medica TIMESTAMP,
    avaliacao_social TIMESTAMP,
    entrada_do_protocolo DATE,
    ultima_atualizacao TIMESTAMP NOT NULL,
    CONSTRAINT processos_pkey PRIMARY KEY(id),
    CONSTRAINT processo_fk_contrato FOREIGN KEY(contrato_id) REFERENCES liv.contratos(id)
);
