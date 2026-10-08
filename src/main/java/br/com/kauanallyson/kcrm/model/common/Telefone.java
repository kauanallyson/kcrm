package br.com.kauanallyson.kcrm.model.common;

import java.util.Objects;

// Celular com DDD; a forma canônica, no banco e na API, é (xx) xxxxx-xxxx
public record Telefone(String value) {
    private static final int LENGTH = 11;

    public Telefone {
        Objects.requireNonNull(value, "telefone");
        String digits = value.replaceAll("\\D", "");
        if (digits.length() != LENGTH) {
            throw new IllegalArgumentException(
                    "Telefone inválido: informe DDD e celular com 9 dígitos, no formato (xx) xxxxx-xxxx");
        }
        value = "(" + digits.substring(0, 2) + ") " + digits.substring(2, 7) + "-" + digits.substring(7);
    }
}
