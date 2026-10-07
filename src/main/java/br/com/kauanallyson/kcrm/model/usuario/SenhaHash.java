package br.com.kauanallyson.kcrm.model.usuario;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

// Um hash só pode vir de um encoder ou do banco, então uma senha em texto puro não é salva por engano
public final class SenhaHash {
    private final String value;

    private SenhaHash(String value) {
        this.value = Objects.requireNonNull(value, "senhaHash");
    }

    public static SenhaHash encode(String senha, PasswordEncoder encoder) {
        return new SenhaHash(encoder.encode(senha));
    }

    static SenhaHash fromStored(String value) {
        return new SenhaHash(value);
    }

    public boolean matches(String senha, PasswordEncoder encoder) {
        return encoder.matches(senha, value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SenhaHash hash && value.equals(hash.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "SenhaHash[****]";
    }
}
