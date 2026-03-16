ALTER TABLE liv.contratos ADD COLUMN valor numeric(19,2);

ALTER TABLE liv.contratos ADD COLUMN indicacao VARCHAR(255);

ALTER TABLE liv.contratos ADD COLUMN numero VARCHAR(15) NOT NULL;

ALTER TABLE liv.contratos ADD CONSTRAINT contratos_uk_numero UNIQUE (numero);