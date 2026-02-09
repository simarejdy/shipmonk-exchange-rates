package com.shipmonk.testingday.rates;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RateIdConverter implements AttributeConverter<RateId, Long> {

    @Override
    public Long convertToDatabaseColumn(RateId attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public RateId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new RateId(dbData);
    }
}
