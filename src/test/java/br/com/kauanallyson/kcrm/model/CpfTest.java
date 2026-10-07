package br.com.kauanallyson.kcrm.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfTest {
    @Test
    void cpfFormatadoESemFormatacaoSaoIguais() {
        assertThat(new Cpf("529.982.247-25")).isEqualTo(new Cpf("52998224725"));
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
    void emailEhNormalizado() {
        assertThat(new Email("  Alice@Test.COM ")).isEqualTo(new Email("alice@test.com"));
    }
}
