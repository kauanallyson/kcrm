package br.com.kauanallyson.kcrm.model.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfTest {
    @Test
    void cpfFormatadoESemFormatacaoSaoIguais() {
        assertThat(new Cpf("529.982.247-25")).isEqualTo(new Cpf("52998224725"));
    }

    @Test
    void cpfFormatadoEhPreservado() {
        assertThat(new Cpf("529.982.247-25").value()).isEqualTo("529.982.247-25");
    }

    @Test
    void cpfSemFormatacaoEhFormatado() {
        assertThat(new Cpf("52998224725").value()).isEqualTo("529.982.247-25");
    }

    @Test
    void grafiasDiferentesDoMesmoCpfSaoIguais() {
        assertThat(new Cpf(" 529.98224725 ")).isEqualTo(new Cpf("529982247-25"));
    }

    @Test
    void digitoVerificadorInvalidoEhRejeitado() {
        assertThatThrownBy(() -> new Cpf("52998224726")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void digitosRepetidosSaoRejeitados() {
        assertThatThrownBy(() -> new Cpf("11111111111")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cpfNuloOuEmBrancoEhRejeitado() {
        assertThatThrownBy(() -> new Cpf(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O CPF não pode ficar em branco");
        assertThatThrownBy(() -> new Cpf("  ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cpfComLetrasOuOutrosCaracteresEhRejeitado() {
        assertThatThrownBy(() -> new Cpf("529.abc.247-25")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Cpf("52998224725x")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Cpf("529 982 247 25")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cpfComQuantidadeErradaDeDigitosEhRejeitado() {
        assertThatThrownBy(() -> new Cpf("5299822472")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Cpf("529982247250")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toStringNaoExpoeONumero() {
        assertThat(new Cpf("52998224725").toString()).doesNotContain("529").doesNotContain("725");
    }
}
