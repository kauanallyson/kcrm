package br.com.kauanallyson.kcrm.model.common;

import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

// complemento é opcional; numero é texto porque existem valores como "S/N" e "123A"
@Embeddable
public record Endereco(
        @Column(name = "endereco_rua", nullable = false, length = 150)
        String rua,
        @Column(name = "endereco_numero", nullable = false, length = 10)
        String numero,
        @Column(name = "endereco_complemento", length = 100)
        String complemento,
        @Column(name = "endereco_bairro", nullable = false, length = 100)
        String bairro,
        @Column(name = "endereco_cidade", nullable = false, length = 100)
        String cidade,
        @Enumerated(EnumType.STRING)
        @Column(name = "endereco_estado", nullable = false, length = 2)
        Uf estado,
        @Column(name = "endereco_cep", nullable = false, length = 9)
        Cep cep
) {
    public Endereco {
        rua = Strings.requireText(rua, "endereco.rua", "A rua não pode ficar em branco");
        if (rua.length() > 150) {
            throw new ValorInvalidoException("endereco.rua", "A rua deve ter no máximo 150 caracteres");
        }
        if (rua.chars().noneMatch(Character::isLetter)) {
            throw new ValorInvalidoException("endereco.rua", "A rua deve conter pelo menos uma letra");
        }

        numero = Strings.requireText(numero, "endereco.numero", "O número não pode ficar em branco; use S/N se não houver");
        if (numero.length() > 10) {
            throw new ValorInvalidoException("endereco.numero", "O número deve ter no máximo 10 caracteres");
        }

        complemento = complemento == null || complemento.isBlank() ? null : complemento.strip();
        if (complemento != null && complemento.length() > 100) {
            throw new ValorInvalidoException("endereco.complemento", "O complemento deve ter no máximo 100 caracteres");
        }

        bairro = Strings.requireText(bairro, "endereco.bairro", "O bairro não pode ficar em branco");
        if (bairro.length() > 100) {
            throw new ValorInvalidoException("endereco.bairro", "O bairro deve ter no máximo 100 caracteres");
        }
        if (bairro.chars().noneMatch(Character::isLetter)) {
            throw new ValorInvalidoException("endereco.bairro", "O bairro deve conter pelo menos uma letra");
        }

        cidade = Strings.requireText(cidade, "endereco.cidade", "A cidade não pode ficar em branco");
        if (cidade.length() > 100) {
            throw new ValorInvalidoException("endereco.cidade", "A cidade deve ter no máximo 100 caracteres");
        }
        if (cidade.chars().noneMatch(Character::isLetter)) {
            throw new ValorInvalidoException("endereco.cidade", "A cidade deve conter pelo menos uma letra");
        }

        if (estado == null) {
            throw new ValorInvalidoException("endereco.estado", "O estado não pode ficar em branco");
        }
        if (cep == null) {
            throw new ValorInvalidoException("endereco.cep", "O CEP não pode ficar em branco");
        }
    }

    public static Endereco de(String rua, String numero, String complemento, String bairro, String cidade,
                              String estado, String cep) {
        return new Endereco(rua, numero, complemento, bairro, cidade, Uf.daSigla(estado), new Cep(cep));
    }
}
