CREATE TABLE liv.financeiros (
    id BIGSERIAL NOT NULL,
	parcelas_restantes INTEGER NOT NULL,
	parcelas_pagas INTEGER NOT NULL,
	valor_proxima_parcela numeric(19,2),
	montante_pago numeric(19,2) NOT NULL,
	numero_solicitacao_pagamento INTEGER,
	data_pagamento_parcela DATE,
	valor_total_pagar numeric(19,2),
	valor_pago_da_parcela numeric(19,2),
	situacao_parcela VARCHAR(30),
	situacao_pagamento BOOLEAN,
    contrato_id int8 NOT NULL,
    CONSTRAINT financeiros_pkey PRIMARY KEY(id),
    CONSTRAINT financeiro_fk_cliente FOREIGN KEY(contrato_id) REFERENCES liv.contratos(id)
);

ALTER TABLE liv.processos ADD COLUMN valor_concedido numeric(19,2);