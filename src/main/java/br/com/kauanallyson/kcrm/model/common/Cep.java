package br.com.kauanallyson.kcrm.model.common;

import java.util.Objects;

// A forma canônica, no banco e na API, é 00000-000
public record Cep(String value) {
    private static final int LENGTH = 8;

    public Cep {
        Objects.requireNonNull(value, "cep");
        String digits = value.replaceAll("\\D", "");
        if (digits.length() != LENGTH) {
            throw new IllegalArgumentException("CEP inválido: informe 8 dígitos, no formato 00000-000");
        }
        value = digits.substring(0, 5) + "-" + digits.substring(5);
    }
}
