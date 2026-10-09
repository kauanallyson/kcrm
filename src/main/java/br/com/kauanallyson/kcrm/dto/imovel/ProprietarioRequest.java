package br.com.kauanallyson.kcrm.dto.imovel;

import br.com.kauanallyson.kcrm.dto.common.FieldErrors;
import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;
import br.com.kauanallyson.kcrm.model.common.Cpf;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.common.Telefone;
import br.com.kauanallyson.kcrm.model.imovel.Proprietario;

import java.util.function.Supplier;

public record ProprietarioRequest(
        String nome,
        String whatsapp,
        String email,
        String cpf
) {
    public Proprietario toProprietario(FieldErrors errors) {
        Telefone whatsappValido = errors.collect(() -> noCampo("proprietario.whatsapp", () -> new Telefone(whatsapp)));
        Email emailValido = errors.collect(() -> noCampo("proprietario.email", () -> new Email(email)));
        Cpf cpfValido = errors.collect(() -> noCampo("proprietario.cpf", () -> new Cpf(cpf)));
        return errors.collect(() -> new Proprietario(nome, whatsappValido, emailValido, cpfValido));
    }

    // Os tipos de valor apontam o próprio campo; aqui o erro precisa voltar no campo aninhado do JSON
    private static <T> T noCampo(String campo, Supplier<T> factory) {
        try {
            return factory.get();
        } catch (ValorInvalidoException e) {
            throw new ValorInvalidoException(campo, e.getMessage());
        }
    }
}
