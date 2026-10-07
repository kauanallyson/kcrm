package br.com.kauanallyson.kcrm.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {
    private static final PasswordEncoder ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private static final Usuario.Dados DADOS = new Usuario.Dados(
            "Alice", new Cpf("52998224725"), new Email("alice@test.com"), "1", "a");

    @Test
    void senhaCadastradaEhGuardadaComoHash() {
        Usuario usuario = Usuario.cadastrar(DADOS, "segredo123", ENCODER);

        assertThat(usuario.getSenhaHash().value()).doesNotContain("segredo123");
        assertThat(usuario.senhaConfere("segredo123", ENCODER)).isTrue();
    }

    @Test
    void senhaInalteradaMantemOMesmoHash() {
        Usuario usuario = Usuario.cadastrar(DADOS, "segredo123", ENCODER);
        SenhaHash antes = usuario.getSenhaHash();

        usuario.alterarSenha("segredo123", ENCODER);

        assertThat(usuario.getSenhaHash()).isSameAs(antes);
    }

    @Test
    void novaSenhaGeraNovoHash() {
        Usuario usuario = Usuario.cadastrar(DADOS, "segredo123", ENCODER);

        usuario.alterarSenha("outro-segredo", ENCODER);

        assertThat(usuario.senhaConfere("outro-segredo", ENCODER)).isTrue();
        assertThat(usuario.senhaConfere("segredo123", ENCODER)).isFalse();
    }
}
