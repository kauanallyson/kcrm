package br.com.kauanallyson.kcrm.model.cliente;

import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.common.Telefone;
import br.com.kauanallyson.kcrm.model.corretor.Corretor;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClienteTest {
    private static final PasswordEncoder ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private static final Telefone WHATSAPP = new Telefone("88999990000");

    static Corretor corretor() {
        return Corretor.cadastrar(new Corretor.Dados("Bruno", new Email("bruno@test.com"), "CRECI-CE 1234", WHATSAPP),
                "segredo123", ENCODER);
    }

    private static Cliente.Dados dados(Origem origem, String indicadoPor) {
        return new Cliente.Dados("  Maria  ", WHATSAPP, origem, indicadoPor, null, null, null);
    }

    @Test
    void cadastroGuardaNomeSemEspacosEOCorretor() {
        Corretor corretor = corretor();
        Cliente cliente = Cliente.cadastrar(dados(Origem.SITE, null), corretor);

        assertThat(cliente.getNome()).isEqualTo("Maria");
        assertThat(cliente.getCorretor()).isSameAs(corretor);
        assertThat(cliente.getIndicadoPor()).isNull();
        assertThat(cliente.getCpf()).isNull();
    }

    @Test
    void indicadoPorEhObrigatorioNaIndicacao() {
        assertThatThrownBy(() -> Cliente.cadastrar(dados(Origem.INDICACAO, "  "), corretor()))
                .isInstanceOf(ValorInvalidoException.class)
                .extracting("campo").isEqualTo("indicadoPor");
    }

    @Test
    void indicadoPorEhProibidoForaDaIndicacao() {
        assertThatThrownBy(() -> Cliente.cadastrar(dados(Origem.INSTAGRAM, "João"), corretor()))
                .isInstanceOf(ValorInvalidoException.class)
                .extracting("campo").isEqualTo("indicadoPor");
    }

    @Test
    void indicadoPorEhNormalizado() {
        Cliente cliente = Cliente.cadastrar(dados(Origem.INDICACAO, "  joão   DA silva e souza "), corretor());

        assertThat(cliente.getIndicadoPor()).isEqualTo("João da Silva e Souza");
    }

    @Test
    void particulaNoInicioDoIndicadoPorEhCapitalizada() {
        Cliente cliente = Cliente.cadastrar(dados(Origem.INDICACAO, "de LUCA"), corretor());

        assertThat(cliente.getIndicadoPor()).isEqualTo("De Luca");
    }

    @Test
    void nomeEOrigemSaoObrigatorios() {
        assertThatThrownBy(() -> Cliente.cadastrar(
                new Cliente.Dados(" ", WHATSAPP, Origem.SITE, null, null, null, null), corretor()))
                .isInstanceOf(ValorInvalidoException.class).extracting("campo").isEqualTo("nome");
        assertThatThrownBy(() -> Cliente.cadastrar(dados(null, null), corretor()))
                .isInstanceOf(ValorInvalidoException.class).extracting("campo").isEqualTo("origem");
    }
}
