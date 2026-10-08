package br.com.kauanallyson.kcrm.model.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {
    @Test
    void emailEhAparadoEMinusculo() {
        assertThat(new Email("  Alice@Test.COM ")).isEqualTo(new Email("alice@test.com"));
        assertThat(new Email("alice.silva+crm@mail.test.com.br").value()).isEqualTo("alice.silva+crm@mail.test.com.br");
    }

    @Test
    void emailNuloOuEmBrancoEhRejeitado() {
        assertThatThrownBy(() -> new Email(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O e-mail não pode ficar em branco");
        assertThatThrownBy(() -> new Email("   ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void emailSemArrobaOuComMaisDeUmaEhRejeitado() {
        assertThatThrownBy(() -> new Email("alice.test.com")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Email("alice@x@test.com")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void emailSemParteLocalEhRejeitado() {
        assertThatThrownBy(() -> new Email("@test.com")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dominioSemPontoOuMalformadoEhRejeitado() {
        assertThatThrownBy(() -> new Email("alice@test")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Email("alice@")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Email("alice@.com")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Email("alice@test.")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Email("alice@test..com")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void emailComEspacoEhRejeitado() {
        assertThatThrownBy(() -> new Email("alice silva@test.com")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void emailAcimaDe254CaracteresEhRejeitado() {
        String dominio = "@test.com";
        assertThat(new Email("a".repeat(254 - dominio.length()) + dominio).value()).hasSize(254);
        assertThatThrownBy(() -> new Email("a".repeat(255 - dominio.length()) + dominio))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O e-mail deve ter no máximo 254 caracteres");
    }
}
