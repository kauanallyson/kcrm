package br.com.kauanallyson.kcrm.model.imovel;

import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;
import br.com.kauanallyson.kcrm.model.common.Cpf;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.common.Telefone;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// Dono do Imóvel; não é um Cliente e fica guardado no próprio Imóvel
@Embeddable
public record Proprietario(
        @Column(name = "proprietario_nome", nullable = false)
        String nome,
        @Column(name = "proprietario_whatsapp", nullable = false, length = 15)
        Telefone whatsapp,
        @Column(name = "proprietario_email", nullable = false)
        Email email,
        @Column(name = "proprietario_cpf", nullable = false, length = 14)
        Cpf cpf
) {
    public Proprietario {
        if (nome == null || nome.isBlank()) {
            throw new ValorInvalidoException("proprietario.nome", "O nome do Proprietário não pode ficar em branco");
        }
        nome = nome.strip();
        if (nome.length() > 255) {
            throw new ValorInvalidoException("proprietario.nome", "O nome do Proprietário deve ter no máximo 255 caracteres");
        }
        if (whatsapp == null) {
            throw new ValorInvalidoException("proprietario.whatsapp", "O WhatsApp do Proprietário não pode ficar em branco");
        }
        if (email == null) {
            throw new ValorInvalidoException("proprietario.email", "O e-mail do Proprietário não pode ficar em branco");
        }
        if (cpf == null) {
            throw new ValorInvalidoException("proprietario.cpf", "O CPF do Proprietário não pode ficar em branco");
        }
    }
}
