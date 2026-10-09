package br.com.kauanallyson.kcrm.dto.cliente;

import br.com.kauanallyson.kcrm.dto.common.EnderecoRequest;
import br.com.kauanallyson.kcrm.dto.common.FieldErrors;
import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;
import br.com.kauanallyson.kcrm.model.cliente.Cliente;
import br.com.kauanallyson.kcrm.model.cliente.Origem;
import br.com.kauanallyson.kcrm.model.common.Cpf;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.common.Endereco;
import br.com.kauanallyson.kcrm.model.common.Telefone;

public record ClienteRequest(
        String nome,
        String whatsapp,
        Origem origem,
        String indicadoPor,
        String cpf,
        String email,
        EnderecoRequest endereco
) {
    // O WhatsApp é um Telefone, mas o erro precisa voltar no campo do JSON do Cliente
    private static Telefone whatsapp(String valor) {
        try {
            return new Telefone(valor);
        } catch (ValorInvalidoException e) {
            throw new ValorInvalidoException("whatsapp", e.getMessage());
        }
    }

    // Obrigatórios e formatos voltam todos juntos; cpf, email e endereco são opcionais
    public Cliente.Dados toDados() {
        FieldErrors errors = new FieldErrors();
        errors.exigir("nome", nome, "O nome não pode ficar em branco");
        errors.exigir("origem", origem, "A origem não pode ficar em branco");
        Telefone whatsappValido = errors.collect(() -> whatsapp(whatsapp));
        Cpf cpfValido = cpf == null || cpf.isBlank() ? null : errors.collect(() -> new Cpf(cpf));
        Email emailValido = email == null || email.isBlank() ? null : errors.collect(() -> new Email(email));
        Endereco enderecoValido = endereco == null ? null : endereco.toEndereco(errors);
        errors.throwIfAny();
        return new Cliente.Dados(nome, whatsappValido, origem, indicadoPor, cpfValido, emailValido, enderecoValido);
    }
}
