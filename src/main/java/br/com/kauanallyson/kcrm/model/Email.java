package br.com.kauanallyson.kcrm.model;

import java.util.Locale;
import java.util.Objects;

public record Email(String value) {
    public Email {
        Objects.requireNonNull(value, "email");
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.isEmpty()) {
            throw new IllegalArgumentException("O e-mail não pode ficar em branco");
        }
    }
}
