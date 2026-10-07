package br.com.kauanallyson.kcrm.model.usuario;

import br.com.kauanallyson.kcrm.exception.MotivoObrigatorioException;
import br.com.kauanallyson.kcrm.model.common.Cpf;
import br.com.kauanallyson.kcrm.model.common.Email;
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
            "Alice", new Cpf("52998224725"), new Email("alice@test.com"), "1", "a");

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

        assertThatThrownBy(() -> admin.desativar("saiu", admin, 1))
                .isInstanceOf(UltimoAdminException.class);
        assertThat(admin.isAtivo()).isTrue();
    }

    @Test
    void adminPodeSeDesativarSeHouverOutroAdminAtivo() {
        Usuario admin = admin();

        admin.desativar("saiu", admin, 2);

        assertThat(admin.isAtivo()).isFalse();
    }

    @Test
    void desativacaoRegistraHistoricoComMotivoEAdmin() {
        Usuario usuario = corretor();
        Usuario admin = admin();

        HistoricoAcesso historico = usuario.desativar("  Saiu da imobiliária ", admin, 1);

        assertThat(usuario.isAtivo()).isFalse();
        assertThat(historico.getTipo()).isEqualTo(TipoMovimentacao.DESATIVACAO);
        assertThat(historico.getMotivo()).isEqualTo("Saiu da imobiliária");
        assertThat(historico.getUsuario()).isSameAs(usuario);
        assertThat(historico.getAdmin()).isSameAs(admin);
    }

    @Test
    void desativacaoExigeMotivo() {
        Usuario usuario = corretor();

        assertThatThrownBy(() -> usuario.desativar(null, admin(), 1)).isInstanceOf(MotivoObrigatorioException.class);
        assertThatThrownBy(() -> usuario.desativar("  ", admin(), 1)).isInstanceOf(MotivoObrigatorioException.class);
        assertThat(usuario.isAtivo()).isTrue();
    }

    @Test
    void usuarioDesativadoNaoPodeSerDesativadoDeNovo() {
        Usuario usuario = corretor();
        usuario.desativar("saiu", admin(), 1);

        assertThatThrownBy(() -> usuario.desativar("de novo", admin(), 1))
                .isInstanceOf(UsuarioJaDesativadoException.class);
    }

    @Test
    void reativacaoDevolveOAcessoERegistraHistorico() {
        Usuario usuario = corretor();
        Usuario admin = admin();
        usuario.desativar("saiu", admin, 1);

        HistoricoAcesso historico = usuario.reativar("voltou", admin);

        assertThat(usuario.isAtivo()).isTrue();
        assertThat(historico.getTipo()).isEqualTo(TipoMovimentacao.REATIVACAO);
        assertThat(historico.getMotivo()).isEqualTo("voltou");
        assertThat(historico.getAdmin()).isSameAs(admin);
    }

    @Test
    void reativacaoSemMotivoEhPermitida() {
        Usuario usuario = corretor();
        usuario.desativar("saiu", admin(), 1);

        assertThat(usuario.reativar(null, admin()).getMotivo()).isNull();
    }

    @Test
    void usuarioAtivoNaoPodeSerReativado() {
        assertThatThrownBy(() -> corretor().reativar(null, admin()))
                .isInstanceOf(UsuarioJaAtivoException.class);
    }

    @Test
    void adminDesativadoNaoContaComoAdminAtivo() {
        Usuario admin = admin();
        admin.desativar("saiu", admin, 2);

        assertThat(admin.isAdminAtivo()).isFalse();
    }
}
