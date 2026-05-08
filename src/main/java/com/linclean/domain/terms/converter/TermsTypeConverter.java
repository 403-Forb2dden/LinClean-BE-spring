package com.linclean.domain.terms.converter;

import com.linclean.domain.terms.entity.TermsType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TermsTypeConverter implements AttributeConverter<TermsType, String> {

    @Override
    public String convertToDatabaseColumn(TermsType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public TermsType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TermsType.from(dbData);
    }
}
