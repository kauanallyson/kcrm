package br.com.kauanallyson.kcrm.model.cliente;

import br.com.kauanallyson.kcrm.exception.CorretorInvalidoException;
import br.com.kauanallyson.kcrm.exception.TransferenciaParaOMesmoCorretorException;
import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;
import br.com.kauanallyson.kcrm.model.common.Cpf;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.common.Endereco;
import br.com.kauanallyson.kcrm.model.common.Telefone;
import br.com.kauanallyson.kcrm.model.usuario.Perfil;
import br.com.kauanallyson.kcrm.model.usuario.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClienteTest {
    private static final PasswordEncoder ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private static final Telefone WHATSAPP = new Telefone("88999990000");

    private static Usuario usuario(Perfil perfil) {
        return Usuario.cadastrar(new Usuario.Dados("Bruno", new Cpf("52998224725"), new Email("bruno@test.com"),
                WHATSAPP, Endereco.de("Rua A", "1", null, "Centro", "Sobral", "CE", "62010000")),
                perfil, "segredo123", ENCODER);
    }

    private static Cliente.Dados dados(Origem origem, String indicadoPor) {
        return new Cliente.Dados("  Maria  ", WHATSAPP, origem, indicadoPor, null, null, null);
    }

    @Test
    void cadastroGuardaNomeSemEspacosEOCorretor() {
        Usuario corretor = usuario(Perfil.CORRETOR);
        Cliente cliente = Cliente.cadastrar(dados(Origem.SITE, null), corretor);

        assertThat(cliente.getNome()).isEqualTo("Maria");
        assertThat(cliente.getCorretor()).isSameAs(corretor);
        assertThat(cliente.getIndicadoPor()).isNull();
        assertThat(cliente.getCpf()).isNull();
    }

    @Test
    void indicadoPorEhObrigatorioNaIndicacao() {
        assertThatThrownBy(() -> Cliente.cadastrar(dados(Origem.INDICACAO, "  "), usuario(Perfil.CORRETOR)))
                .isInstanceOf(ValorInvalidoException.class)
                .extracting("campo").isEqualTo("indicadoPor");
    }

    @Test
    void indicadoPorEhProibidoForaDaIndicacao() {
        assertThatThrownBy(() -> Cliente.cadastrar(dados(Origem.INSTAGRAM, "João"), usuario(Perfil.CORRETOR)))
                .isInstanceOf(ValorInvalidoException.class)
                .extracting("campo").isEqualTo("indicadoPor");
    }

    @Test
    void indicadoPorEhNormalizado() {
        Cliente cliente = Cliente.cadastrar(dados(Origem.INDICACAO, "  joão   DA silva e souza "), usuario(Perfil.CORRETOR));

        assertThat(cliente.getIndicadoPor()).isEqualTo("João da Silva e Souza");
    }

    @Test
    void particulaNoInicioDoIndicadoPorEhCapitalizada() {
        Cliente cliente = Cliente.cadastrar(dados(Origem.INDICACAO, "de LUCA"), usuario(Perfil.CORRETOR));

        assertThat(cliente.getIndicadoPor()).isEqualTo("De Luca");
    }

    @Test
    void nomeEOrigemSaoObrigatorios() {
        assertThatThrownBy(() -> Cliente.cadastrar(
                new Cliente.Dados(" ", WHATSAPP, Origem.SITE, null, null, null, null), usuario(Perfil.CORRETOR)))
                .isInstanceOf(ValorInvalidoException.class).extracting("campo").isEqualTo("nome");
        assertThatThrownBy(() -> Cliente.cadastrar(dados(null, null), usuario(Perfil.CORRETOR)))
                .isInstanceOf(ValorInvalidoException.class).extracting("campo").isEqualTo("origem");
    }

    @Test
    void corretorPrecisaSerCorretorAtivo() {
        Usuario desativado = usuario(Perfil.CORRETOR);
        desativado.desativar("saiu", 1);

        assertThatThrownBy(() -> Cliente.cadastrar(dados(Origem.SITE, null), usuario(Perfil.ADMIN)))
                .isInstanceOf(CorretorInvalidoException.class);
        assertThatThrownBy(() -> Cliente.cadastrar(dados(Origem.SITE, null), desativado))
                .isInstanceOf(CorretorInvalidoException.class);
    }

    @Test
    void transferenciaTrocaOCorretorEDevolveOAnterior() {
        Usuario anterior = usuario(Perfil.CORRETOR);
        Usuario novo = usuario(Perfil.CORRETOR);
        Cliente cliente = Cliente.cadastrar(dados(Origem.SITE, null), anterior);

        assertThat(cliente.transferir(novo)).isSameAs(anterior);
        assertThat(cliente.getCorretor()).isSameAs(novo);
    }

    @Test
    void transferenciaParaOMesmoCorretorEhRejeitada() {
        Usuario corretor = usuario(Perfil.CORRETOR);
        Cliente cliente = Cliente.cadastrar(dados(Origem.SITE, null), corretor);

        assertThatThrownBy(() -> cliente.transferir(corretor))
                .isInstanceOf(TransferenciaParaOMesmoCorretorException.class);
    }

    @Test
    void transferenciaExigeCorretorAtivo() {
        Cliente cliente = Cliente.cadastrar(dados(Origem.SITE, null), usuario(Perfil.CORRETOR));

        assertThatThrownBy(() -> cliente.transferir(usuario(Perfil.ADMIN)))
                .isInstanceOf(CorretorInvalidoException.class);
    }
}
