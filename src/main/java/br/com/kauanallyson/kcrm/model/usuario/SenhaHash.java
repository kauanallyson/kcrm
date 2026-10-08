package br.com.kauanallyson.kcrm.model.usuario;

import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;

import java.util.Objects;

// Um hash só pode vir de um encoder ou do banco, então uma senha em texto puro não é salva por engano
public final class SenhaHash {
    private static final int MINIMO = 8;
    private static final int MAXIMO_BYTES = 72;

    private final String value;

    private SenhaHash(String value) {
        this.value = Objects.requireNonNull(value, "senhaHash");
    }

    // Sem strip: espaços fazem parte da senha. 72 bytes é o limite do BCrypt
    public static SenhaHash encode(String senha, PasswordEncoder encoder) {
        if (senha == null || senha.isBlank()) {
            throw new ValorInvalidoException("senha", "A senha não pode ficar em branco");
        }
        if (senha.length() < MINIMO || senha.getBytes(StandardCharsets.UTF_8).length > MAXIMO_BYTES) {
            throw new ValorInvalidoException("senha", "A senha deve ter entre 8 e 72 caracteres");
        }
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
