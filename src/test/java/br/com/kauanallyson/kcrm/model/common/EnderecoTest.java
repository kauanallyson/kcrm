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
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("CEP");
        assertThatThrownBy(() -> new Endereco("Rua A", "1", null, "Centro", "Sobral", null, new Cep("62010000")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("estado");
    }

    @Test
    void camposAcimaDoTamanhoMaximoSaoRejeitados() {
        assertThatThrownBy(() -> endereco("R".repeat(151), "1", null, "Centro", "Sobral", "CE", "62010000"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("A rua deve ter no máximo 150 caracteres");
        assertThatThrownBy(() -> endereco("Rua A", "12345678901", null, "Centro", "Sobral", "CE", "62010000"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("O número deve ter no máximo 10 caracteres");
        assertThatThrownBy(() -> endereco("Rua A", "1", "C".repeat(101), "Centro", "Sobral", "CE", "62010000"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("O complemento deve ter no máximo 100 caracteres");
        assertThatThrownBy(() -> endereco("Rua A", "1", null, "B".repeat(101), "Sobral", "CE", "62010000"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("O bairro deve ter no máximo 100 caracteres");
        assertThatThrownBy(() -> endereco("Rua A", "1", null, "Centro", "C".repeat(101), "CE", "62010000"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("A cidade deve ter no máximo 100 caracteres");
    }

    @Test
    void camposNoTamanhoMaximoSaoAceitos() {
        Endereco endereco = endereco("R".repeat(150), "1234567890", "C".repeat(100), "B".repeat(100),
                "C".repeat(100), "CE", "62010000");

        assertThat(endereco.rua()).hasSize(150);
    }

    @Test
    void ruaBairroECidadeSemLetraSaoRejeitados() {
        assertThatThrownBy(() -> endereco("123", "1", null, "Centro", "Sobral", "CE", "62010000"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("A rua deve conter pelo menos uma letra");
        assertThatThrownBy(() -> endereco("Rua A", "1", null, "--", "Sobral", "CE", "62010000"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("O bairro deve conter pelo menos uma letra");
        assertThatThrownBy(() -> endereco("Rua A", "1", null, "Centro", "42", "CE", "62010000"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("A cidade deve conter pelo menos uma letra");
    }

    @Test
    void ufAceitaMinusculasEDemaisSiglasSaoRejeitadas() {
        assertThat(Uf.daSigla(" sp ")).isEqualTo(Uf.SP);
        assertThatThrownBy(() -> Uf.daSigla(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O estado não pode ficar em branco");
        assertThatThrownBy(() -> Uf.daSigla("BR")).isInstanceOf(IllegalArgumentException.class);
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
