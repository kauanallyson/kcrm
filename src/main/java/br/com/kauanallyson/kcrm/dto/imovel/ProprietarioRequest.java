package br.com.kauanallyson.kcrm.dto.imovel;

import br.com.kauanallyson.kcrm.model.common.Cpf;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.common.FieldErrors;
import br.com.kauanallyson.kcrm.model.common.Telefone;
import br.com.kauanallyson.kcrm.model.imovel.Proprietario;

public record ProprietarioRequest(
        String nome,
        String whatsapp,
        String email,
        String cpf
) {
    public Proprietario toProprietario(FieldErrors errors) {
        Telefone whatsappValido = errors.collect("proprietario.whatsapp", () -> new Telefone(whatsapp));
        Email emailValido = errors.collect("proprietario.email", () -> new Email(email));
        Cpf cpfValido = errors.collect("proprietario.cpf", () -> new Cpf(cpf));
        return errors.collect(() -> new Proprietario(nome, whatsappValido, emailValido, cpfValido));
    }
}
