package br.com.kauanallyson.kcrm.model.usuario;

import br.com.kauanallyson.kcrm.exception.MotivoObrigatorioException;
import br.com.kauanallyson.kcrm.model.common.Cpf;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.common.Telefone;
import br.com.kauanallyson.kcrm.exception.UltimoAdminException;
import br.com.kauanallyson.kcrm.exception.UsuarioJaAtivoException;
import br.com.kauanallyson.kcrm.exception.UsuarioJaDesativadoException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuarioTest {
    private static final PasswordEncoder ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private static final Usuario.Dados DADOS = new Usuario.Dados(
            "Alice", new Cpf("52998224725"), new Email("alice@test.com"), new Telefone("88999990000"), "a");

    private static Usuario corretor() {
        return Usuario.cadastrar(DADOS, Perfil.CORRETOR, "segredo123", ENCODER);
    }

    private static Usuario admin() {
        return Usuario.cadastrar(DADOS, Perfil.ADMIN, "segredo123", ENCODER);
    }

    @Test
    void senhaCadastradaEhGuardadaComoHash() {
        Usuario usuario = corretor();

        assertThat(usuario.getSenhaHash().value()).doesNotContain("segredo123");
        assertThat(usuario.senhaConfere("segredo123", ENCODER)).isTrue();
    }

    @Test
    void usuarioCadastradoTemOPerfilEscolhidoEEstaAtivo() {
        assertThat(corretor().getPerfil()).isEqualTo(Perfil.CORRETOR);
        assertThat(admin().getPerfil()).isEqualTo(Perfil.ADMIN);
        assertThat(corretor().isAtivo()).isTrue();
    }

    @Test
    void senhaInalteradaMantemOMesmoHash() {
        Usuario usuario = corretor();
        SenhaHash antes = usuario.getSenhaHash();

        usuario.alterarSenha("segredo123", ENCODER);

        assertThat(usuario.getSenhaHash()).isSameAs(antes);
    }

    @Test
    void novaSenhaGeraNovoHash() {
        Usuario usuario = corretor();

        usuario.alterarSenha("outro-segredo", ENCODER);

        assertThat(usuario.senhaConfere("outro-segredo", ENCODER)).isTrue();
        assertThat(usuario.senhaConfere("segredo123", ENCODER)).isFalse();
    }

    @Test
    void ultimoAdminAtivoNaoPodeVirarCorretor() {
        Usuario admin = admin();

        assertThatThrownBy(() -> admin.mudarPerfil(Perfil.CORRETOR, 1))
                .isInstanceOf(UltimoAdminException.class);
        assertThat(admin.getPerfil()).isEqualTo(Perfil.ADMIN);
    }

    @Test
    void adminViraCorretorSeHouverOutroAdminAtivo() {
        Usuario admin = admin();

        admin.mudarPerfil(Perfil.CORRETOR, 2);

        assertThat(admin.getPerfil()).isEqualTo(Perfil.CORRETOR);
    }

    @Test
    void corretorViraAdmin() {
        Usuario usuario = corretor();

        usuario.mudarPerfil(Perfil.ADMIN, 1);

        assertThat(usuario.getPerfil()).isEqualTo(Perfil.ADMIN);
    }

    @Test
    void ultimoAdminAtivoNaoPodeSerDesativado() {
        Usuario admin = admin();

        assertThatThrownBy(() -> admin.desativar("saiu", 1))
                .isInstanceOf(UltimoAdminException.class);
        assertThat(admin.isAtivo()).isTrue();
    }

    @Test
    void adminPodeSeDesativarSeHouverOutroAdminAtivo() {
        Usuario admin = admin();

        admin.desativar("saiu", 2);

        assertThat(admin.isAtivo()).isFalse();
    }

    @Test
    void desativacaoTiraOAcesso() {
        Usuario usuario = corretor();

        usuario.desativar("Saiu da imobiliária", 1);

        assertThat(usuario.isAtivo()).isFalse();
    }

    @Test
    void mudarParaOMesmoPerfilNaoEhMudanca() {
        Usuario usuario = corretor();

        assertThat(usuario.mudarPerfil(Perfil.CORRETOR, 1)).isFalse();
        assertThat(usuario.mudarPerfil(Perfil.ADMIN, 1)).isTrue();
        assertThat(usuario.getPerfil()).isEqualTo(Perfil.ADMIN);
    }

    @Test
    void ultimoAdminPodeSerMantidoComoAdmin() {
        assertThat(admin().mudarPerfil(Perfil.ADMIN, 1)).isFalse();
    }

    @Test
    void desativacaoExigeMotivo() {
        Usuario usuario = corretor();

        assertThatThrownBy(() -> usuario.desativar(null, 1)).isInstanceOf(MotivoObrigatorioException.class);
        assertThatThrownBy(() -> usuario.desativar("  ", 1)).isInstanceOf(MotivoObrigatorioException.class);
        assertThat(usuario.isAtivo()).isTrue();
    }

    @Test
    void usuarioDesativadoNaoPodeSerDesativadoDeNovo() {
        Usuario usuario = corretor();
        usuario.desativar("saiu", 1);

        assertThatThrownBy(() -> usuario.desativar("de novo", 1))
                .isInstanceOf(UsuarioJaDesativadoException.class);
    }

    @Test
    void reativacaoDevolveOAcesso() {
        Usuario usuario = corretor();
        usuario.desativar("saiu", 1);

        usuario.reativar();

        assertThat(usuario.isAtivo()).isTrue();
    }

    @Test
    void usuarioAtivoNaoPodeSerReativado() {
        assertThatThrownBy(() -> corretor().reativar())
                .isInstanceOf(UsuarioJaAtivoException.class);
    }

    @Test
    void adminDesativadoNaoContaComoAdminAtivo() {
        Usuario admin = admin();
        admin.desativar("saiu", 2);

        assertThat(admin.isAdminAtivo()).isFalse();
    }
}
