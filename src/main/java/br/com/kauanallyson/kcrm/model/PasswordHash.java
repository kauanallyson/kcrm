package br.com.kauanallyson.kcrm.model;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

// A hash can only come from an encoder or from storage, so a raw password can't be saved by mistake
public final class PasswordHash {
    private final String value;

    private PasswordHash(String value) {
        this.value = Objects.requireNonNull(value, "passwordHash");
    }

    public static PasswordHash encode(String rawPassword, PasswordEncoder encoder) {
        return new PasswordHash(encoder.encode(rawPassword));
    }

    static PasswordHash fromStored(String value) {
        return new PasswordHash(value);
    }

    public boolean matches(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PasswordHash hash && value.equals(hash.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "PasswordHash[****]";
    }
}
