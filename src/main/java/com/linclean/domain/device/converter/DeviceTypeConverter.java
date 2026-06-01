package com.linclean.domain.device.converter;

import com.linclean.domain.device.entity.DeviceType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DeviceTypeConverter implements AttributeConverter<DeviceType, String> {

    @Override
    public String convertToDatabaseColumn(DeviceType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public DeviceType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : DeviceType.from(dbData);
    }
}
