package br.com.kauanallyson.kcrm.model.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnderecoTest {
    private static Endereco endereco(String rua, String numero, String complemento, String bairro, String cidade,
                                     String estado, String cep) {
        return Endereco.de(rua, numero, complemento, bairro, cidade, estado, cep);
    }

    @Test
    void camposSaoAparadosEEstadoECepNormalizados() {
        Endereco endereco = endereco(" Rua A ", " S/N ", " Apto 2 ", " Centro ", " Sobral ", " ce ", "62010000");

        assertThat(endereco).isEqualTo(new Endereco("Rua A", "S/N", "Apto 2", "Centro", "Sobral", Uf.CE,
                new Cep("62010-000")));
    }

    @Test
    void numeroAceitaTexto() {
        assertThat(endereco("Rua A", "123A", null, "Centro", "Sobral", "CE", "62010000").numero()).isEqualTo("123A");
    }

    @Test
    void complementoAusenteFicaNulo() {
        assertThat(endereco("Rua A", "1", null, "Centro", "Sobral", "CE", "62010000").complemento()).isNull();
    }

    @Test
    void complementoEmBrancoFicaNulo() {
        assertThat(endereco("Rua A", "1", "   ", "Centro", "Sobral", "CE", "62010000").complemento()).isNull();
    }

    @Test
    void camposObrigatoriosEmBrancoSaoRejeitados() {
        assertThatThrownBy(() -> endereco(" ", "1", null, "Centro", "Sobral", "CE", "62010000"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("rua");
        assertThatThrownBy(() -> endereco("Rua A", null, null, "Centro", "Sobral", "CE", "62010000"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("número");
        assertThatThrownBy(() -> endereco("Rua A", "1", null, "", "Sobral", "CE", "62010000"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("bairro");
        assertThatThrownBy(() -> endereco("Rua A", "1", null, "Centro", " ", "CE", "62010000"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("cidade");
        assertThatThrownBy(() -> endereco("Rua A", "1", null, "Centro", "Sobral", "CE", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void estadoForaDasVinteESeteUfsEhRejeitado() {
        assertThatThrownBy(() -> endereco("Rua A", "1", null, "Centro", "Sobral", "XX", "62010000"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("UF");
        assertThatThrownBy(() -> endereco("Rua A", "1", null, "Centro", "Sobral", "Ceará", "62010000"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void existemVinteESeteUfs() {
        assertThat(Uf.values()).hasSize(27);
    }
}
