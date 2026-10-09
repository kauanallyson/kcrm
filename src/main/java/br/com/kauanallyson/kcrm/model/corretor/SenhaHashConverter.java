package br.com.kauanallyson.kcrm.model.corretor;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
class SenhaHashConverter implements AttributeConverter<SenhaHash, String> {
    @Override
    public String convertToDatabaseColumn(SenhaHash hash) {
        return hash == null ? null : hash.value();
    }

    @Override
    public SenhaHash convertToEntityAttribute(String value) {
        return value == null ? null : SenhaHash.fromStored(value);
    }
}
