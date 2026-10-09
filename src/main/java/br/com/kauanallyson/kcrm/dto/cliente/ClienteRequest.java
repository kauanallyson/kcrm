package br.com.kauanallyson.kcrm.dto.cliente;

import br.com.kauanallyson.kcrm.dto.common.EnderecoRequest;
import br.com.kauanallyson.kcrm.model.cliente.Cliente;
import br.com.kauanallyson.kcrm.model.cliente.Origem;
import br.com.kauanallyson.kcrm.model.common.Cpf;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.common.Endereco;
import br.com.kauanallyson.kcrm.model.common.FieldErrors;
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
    // Formatos e regras do Cliente voltam todos juntos; cpf, email e endereco são opcionais
    public Cliente.Dados toDados() {
        FieldErrors errors = new FieldErrors();
        Telefone whatsappValido = whatsapp == null || whatsapp.isBlank() ? null
                : errors.collect("whatsapp", () -> new Telefone(whatsapp));
        Cpf cpfValido = cpf == null || cpf.isBlank() ? null : errors.collect(() -> new Cpf(cpf));
        Email emailValido = email == null || email.isBlank() ? null : errors.collect(() -> new Email(email));
        Endereco enderecoValido = endereco == null ? null : endereco.toEndereco(errors);
        Cliente.Dados dados = errors.collect(() -> new Cliente.Dados(nome, whatsappValido, origem, indicadoPor,
                cpfValido, emailValido, enderecoValido));
        errors.throwIfAny();
        return dados;
    }

    @Override
    public String toString() {
        return "ClienteRequest[nome=" + nome + ", whatsapp=" + whatsapp + ", origem=" + origem
                + ", indicadoPor=" + indicadoPor + ", cpf=" + Cpf.mascarar(cpf) + ", email=" + email
                + ", endereco=" + endereco + "]";
    }
}
