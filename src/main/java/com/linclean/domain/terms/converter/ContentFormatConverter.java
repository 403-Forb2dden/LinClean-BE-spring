package com.linclean.domain.terms.converter;

import com.linclean.domain.terms.entity.ContentFormat;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ContentFormatConverter implements AttributeConverter<ContentFormat, String> {

    @Override
    public String convertToDatabaseColumn(ContentFormat attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ContentFormat convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ContentFormat.from(dbData);
    }
}