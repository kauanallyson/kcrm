package br.com.kauanallyson.kcrm.model.usuario;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SenhaHashTest {
    private static final PasswordEncoder ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Test
    void senhaNulaOuEmBrancoEhRejeitada() {
        assertThatThrownBy(() -> SenhaHash.encode(null, ENCODER)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A senha não pode ficar em branco");
        assertThatThrownBy(() -> SenhaHash.encode("        ", ENCODER)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void senhaCurtaOuLongaDemaisEhRejeitada() {
        assertThatThrownBy(() -> SenhaHash.encode("1234567", ENCODER)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A senha deve ter entre 8 e 72 caracteres");
        assertThatThrownBy(() -> SenhaHash.encode("a".repeat(73), ENCODER)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void senhaNosLimitesEhAceita() {
        assertThat(SenhaHash.encode("12345678", ENCODER).matches("12345678", ENCODER)).isTrue();
        assertThat(SenhaHash.encode("a".repeat(72), ENCODER).matches("a".repeat(72), ENCODER)).isTrue();
    }
}
