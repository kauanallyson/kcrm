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
    void telefoneNuloEhRejeitado() {
        assertThatThrownBy(() -> new Telefone(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O número não pode ficar em branco");
    }

    @Test
    void dddInexistenteEhRejeitado() {
        for (String ddd : new String[]{"10", "20", "23", "25", "26", "29", "30", "36", "39", "40", "50", "52",
                "56", "57", "58", "59", "60", "70", "72", "76", "78", "80", "90"}) {
            assertThatThrownBy(() -> new Telefone(ddd + "999990000"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("DDD " + ddd);
        }
        assertThatThrownBy(() -> new Telefone("01999990000")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dddsExistentesSaoAceitos() {
        for (String ddd : new String[]{"11", "21", "61", "85", "88", "99"}) {
            assertThat(new Telefone(ddd + "999990000").value()).startsWith("(" + ddd + ")");
        }
    }

    @Test
    void celularQueNaoComecaCom9EhRejeitado() {
        assertThatThrownBy(() -> new Telefone("88899990000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Número inválido: o celular deve começar com 9");
    }

    @Test
    void telefoneComLetrasOuOutrosCaracteresEhRejeitado() {
        assertThatThrownBy(() -> new Telefone("(88) 9999a-0000")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Telefone("88.99999.0000")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void telefoneComDigitosAMaisOuEmBrancoEhRejeitado() {
        assertThatThrownBy(() -> new Telefone("+55 88 99999-0000")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Telefone("  ")).isInstanceOf(IllegalArgumentException.class);
    }
}
