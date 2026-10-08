package br.com.kauanallyson.kcrm.model.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Objects;

// complemento é opcional; numero é texto porque existem valores como "S/N" e "123A"
@Embeddable
public record Endereco(
        @Column(name = "endereco_rua", nullable = false)
        String rua,
        @Column(name = "endereco_numero", nullable = false, length = 20)
        String numero,
        @Column(name = "endereco_complemento")
        String complemento,
        @Column(name = "endereco_bairro", nullable = false)
        String bairro,
        @Column(name = "endereco_cidade", nullable = false)
        String cidade,
        @Enumerated(EnumType.STRING)
        @Column(name = "endereco_estado", nullable = false, length = 2)
        Uf estado,
        @Column(name = "endereco_cep", nullable = false, length = 9)
        Cep cep
) {
    public Endereco {
        rua = exigir(rua, "rua");
        numero = exigir(numero, "número");
        bairro = exigir(bairro, "bairro");
        cidade = exigir(cidade, "cidade");
        complemento = complemento == null || complemento.isBlank() ? null : complemento.strip();
        Objects.requireNonNull(estado, "estado");
        Objects.requireNonNull(cep, "cep");
    }

    public static Endereco de(String rua, String numero, String complemento, String bairro, String cidade,
                              String estado, String cep) {
        return new Endereco(rua, numero, complemento, bairro, cidade, Uf.daSigla(estado), new Cep(cep));
    }

    private static String exigir(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("O campo " + campo + " do endereço não pode ficar em branco");
        }
        return valor.strip();
    }
}
