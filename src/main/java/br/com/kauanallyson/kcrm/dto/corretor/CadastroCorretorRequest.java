package br.com.kauanallyson.kcrm.dto.corretor;

import br.com.kauanallyson.kcrm.model.common.FieldErrors;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.common.Telefone;
import br.com.kauanallyson.kcrm.model.corretor.Corretor;
import br.com.kauanallyson.kcrm.model.corretor.SenhaHash;

public record CadastroCorretorRequest(
        String nome,
        String email,
        String senha,
        String creci,
        String whatsapp
) {
    // Obrigatórios, senha e formatos voltam todos juntos, com as mesmas mensagens do domínio
    public Corretor.Dados toDados() {
        FieldErrors errors = new FieldErrors();
        errors.exigir("nome", nome, "O nome não pode ficar em branco");
        errors.exigir("creci", creci, "O CRECI não pode ficar em branco");
        errors.collect(() -> {
            SenhaHash.validar(senha);
            return null;
        });
        Email emailValido = errors.collect(() -> new Email(email));
        Telefone whatsappValido = errors.collect("whatsapp", () -> new Telefone(whatsapp));
        errors.throwIfAny();
        return new Corretor.Dados(nome, emailValido, creci, whatsappValido);
    }

}
