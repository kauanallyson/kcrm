package br.com.kauanallyson.kcrm.model.corretor;

import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;
import br.com.kauanallyson.kcrm.model.common.Email;
import org.junit.jupiter.api.Test;

import static br.com.kauanallyson.kcrm.TestDominio.ENCODER;
import static br.com.kauanallyson.kcrm.TestDominio.WHATSAPP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorretorTest {
    private static final Email EMAIL = new Email("bruno@test.com");

    private static Corretor cadastrar(String nome, String creci) {
        return Corretor.cadastrar(new Corretor.Dados(nome, EMAIL, creci, WHATSAPP), "segredo123", ENCODER);
    }

    @Test
    void cadastroAparaNomeENormalizaCreciEGuardaSenhaEmHash() {
        Corretor corretor = cadastrar("  Bruno  ", "  creci-ce 1234 ");

        assertThat(corretor.getNome()).isEqualTo("Bruno");
        assertThat(corretor.getCreci()).isEqualTo("CRECI-CE 1234");
        assertThat(corretor.getSenhaHash().matches("segredo123", ENCODER)).isTrue();
    }

    @Test
    void creciEmBrancoOuLongoDemaisEhRejeitado() {
        assertThatThrownBy(() -> cadastrar("Bruno", "  "))
                .isInstanceOf(ValorInvalidoException.class).extracting("campo").isEqualTo("creci");
        assertThatThrownBy(() -> cadastrar("Bruno", "1".repeat(21)))
                .isInstanceOf(ValorInvalidoException.class).extracting("campo").isEqualTo("creci");
        assertThat(cadastrar("Bruno", "1".repeat(20)).getCreci()).hasSize(20);
    }

    @Test
    void nomeEmailEWhatsappSaoObrigatorios() {
        assertThatThrownBy(() -> cadastrar(" ", "123"))
                .isInstanceOf(ValorInvalidoException.class).extracting("campo").isEqualTo("nome");
        assertThatThrownBy(() -> Corretor.cadastrar(new Corretor.Dados("Bruno", null, "123", WHATSAPP), "segredo123", ENCODER))
                .isInstanceOf(ValorInvalidoException.class).extracting("campo").isEqualTo("email");
        assertThatThrownBy(() -> Corretor.cadastrar(new Corretor.Dados("Bruno", EMAIL, "123", null), "segredo123", ENCODER))
                .isInstanceOf(ValorInvalidoException.class).extracting("campo").isEqualTo("whatsapp");
    }

    @Test
    void senhaInvalidaEhRejeitada() {
        assertThatThrownBy(() -> Corretor.cadastrar(new Corretor.Dados("Bruno", EMAIL, "123", WHATSAPP), "curta", ENCODER))
                .isInstanceOf(ValorInvalidoException.class).extracting("campo").isEqualTo("senha");
    }
}
