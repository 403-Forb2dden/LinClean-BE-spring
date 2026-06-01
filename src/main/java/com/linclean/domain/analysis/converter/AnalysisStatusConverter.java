package com.linclean.domain.analysis.converter;

import com.linclean.domain.analysis.entity.AnalysisStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AnalysisStatusConverter implements AttributeConverter<AnalysisStatus, String> {

    @Override
    public String convertToDatabaseColumn(AnalysisStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public AnalysisStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AnalysisStatus.from(dbData);
    }
}
