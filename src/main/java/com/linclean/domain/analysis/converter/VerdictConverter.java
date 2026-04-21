package com.linclean.domain.analysis.converter;

import com.linclean.domain.analysis.entity.Verdict;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class VerdictConverter implements AttributeConverter<Verdict, String> {

    @Override
    public String convertToDatabaseColumn(Verdict attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public Verdict convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Verdict.from(dbData);
    }
}
