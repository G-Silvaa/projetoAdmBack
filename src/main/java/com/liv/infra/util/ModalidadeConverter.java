package com.liv.infra.util;

import com.liv.domain.Modalidade;

import jakarta.persistence.AttributeConverter;

public class ModalidadeConverter implements AttributeConverter<Modalidade, String> {
	
	@Override
    public String convertToDatabaseColumn(Modalidade modalidade) {
        return modalidade != null ? modalidade.getModalidade() : null;
    }

    @Override
    public Modalidade convertToEntityAttribute(String modalidadeString) {
        return modalidadeString != null ? Modalidade.fromBeneficio(modalidadeString) : null; // Ajuste o índice
    }

}
