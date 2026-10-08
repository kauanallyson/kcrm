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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// corretorId só vale no cadastro feito pelo Admin; na edição é rejeitado (a troca é por Transferência)
public record ClienteRequest(
        @NotBlank(message = "O nome não pode ficar em branco")
        String nome,
        String whatsapp,
        @NotNull(message = "A origem não pode ficar em branco")
        Origem origem,
        String indicadoPor,
        String cpf,
        String email,
        EnderecoRequest endereco,
        UUID corretorId
) {
    // Formatos são validados pelos próprios tipos de valor; cpf, email e endereco são opcionais
    public Cliente.Dados toDados() {
        FieldErrors errors = new FieldErrors();
        Telefone whatsappValido = errors.collect(() -> whatsapp(whatsapp));
        Cpf cpfValido = cpf == null || cpf.isBlank() ? null : errors.collect(() -> new Cpf(cpf));
        Email emailValido = email == null || email.isBlank() ? null : errors.collect(() -> new Email(email));
        Endereco enderecoValido = endereco == null ? null : endereco.toEndereco(errors);
        errors.throwIfAny();
        return new Cliente.Dados(nome, whatsappValido, origem, indicadoPor, cpfValido, emailValido, enderecoValido);
    }

    // O WhatsApp é um Telefone, mas o erro precisa voltar no campo do JSON do Cliente
    private static Telefone whatsapp(String valor) {
        try {
            return new Telefone(valor);
        } catch (ValorInvalidoException e) {
            throw new ValorInvalidoException("whatsapp", e.getMessage());
        }
    }
}
