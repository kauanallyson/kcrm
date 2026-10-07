package br.com.kauanallyson.kcrm.model.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

final class ValueConverters {
    private ValueConverters() {
    }

    @Converter(autoApply = true)
    static class EmailConverter implements AttributeConverter<Email, String> {
        @Override
        public String convertToDatabaseColumn(Email email) {
            return email == null ? null : email.value();
        }

        @Override
        public Email convertToEntityAttribute(String value) {
            return value == null ? null : new Email(value);
        }
    }

    @Converter(autoApply = true)
    static class CpfConverter implements AttributeConverter<Cpf, String> {
        @Override
        public String convertToDatabaseColumn(Cpf cpf) {
            return cpf == null ? null : cpf.value();
        }

        @Override
        public Cpf convertToEntityAttribute(String value) {
            return value == null ? null : new Cpf(value);
        }
    }
}
