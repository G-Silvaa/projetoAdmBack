package com.liv.infra.util;

import com.liv.domain.Mes;

import jakarta.persistence.AttributeConverter;

public class MesConverter implements AttributeConverter<Mes, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Mes mes) {
        return mes != null ? mes.getOrdinal() : null;
    }

    @Override
    public Mes convertToEntityAttribute(Integer ordinal) {
        return ordinal != null ? Mes.values()[ordinal - 1] : null; // Ajuste o índice
    }
}
