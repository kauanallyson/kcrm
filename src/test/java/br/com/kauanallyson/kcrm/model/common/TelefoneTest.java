package br.com.kauanallyson.kcrm.model.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelefoneTest {
    @Test
    void telefoneFormatadoEhPreservado() {
        assertThat(new Telefone("(88) 99999-0000").value()).isEqualTo("(88) 99999-0000");
    }

    @Test
    void telefoneSemFormatacaoEhFormatado() {
        assertThat(new Telefone("88999990000").value()).isEqualTo("(88) 99999-0000");
    }

    @Test
    void grafiasDiferentesDoMesmoTelefoneSaoIguais() {
        assertThat(new Telefone(" 88 99999 0000 ")).isEqualTo(new Telefone("(88)99999-0000"));
    }

    @Test
    void telefoneFixoComDezDigitosEhRejeitado() {
        assertThatThrownBy(() -> new Telefone("(88) 3611-0000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("(xx) xxxxx-xxxx");
    }

    @Test
    void telefoneComDigitosAMaisOuEmBrancoEhRejeitado() {
        assertThatThrownBy(() -> new Telefone("+55 88 99999-0000")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Telefone("  ")).isInstanceOf(IllegalArgumentException.class);
    }
}
