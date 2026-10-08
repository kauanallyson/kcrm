package br.com.kauanallyson.kcrm.model.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CepTest {
    @Test
    void cepFormatadoEhPreservado() {
        assertThat(new Cep("62010-000").value()).isEqualTo("62010-000");
    }

    @Test
    void cepSemFormatacaoEhFormatado() {
        assertThat(new Cep("62010000").value()).isEqualTo("62010-000");
    }

    @Test
    void grafiasDiferentesDoMesmoCepSaoIguais() {
        assertThat(new Cep(" 62010-000 ")).isEqualTo(new Cep("62010000"));
    }

    @Test
    void cepSemOitoDigitosEhRejeitado() {
        assertThatThrownBy(() -> new Cep("6201000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("00000-000");
        assertThatThrownBy(() -> new Cep("620100001")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Cep(" ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cepNuloEhRejeitado() {
        assertThatThrownBy(() -> new Cep(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O CEP não pode ficar em branco");
    }

    @Test
    void cepComDigitosTodosIguaisEhRejeitado() {
        assertThatThrownBy(() -> new Cep("00000000")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Cep("11111-111")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cepComLetrasOuOutrosCaracteresEhRejeitado() {
        assertThatThrownBy(() -> new Cep("62a10-000")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Cep("62.010-000")).isInstanceOf(IllegalArgumentException.class);
    }
}
