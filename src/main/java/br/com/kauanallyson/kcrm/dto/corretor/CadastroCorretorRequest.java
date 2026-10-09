package br.com.kauanallyson.kcrm.dto.corretor;

import br.com.kauanallyson.kcrm.dto.common.FieldErrors;
import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.common.Telefone;
import br.com.kauanallyson.kcrm.model.corretor.Corretor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroCorretorRequest(
        @NotBlank(message = "O nome não pode ficar em branco")
        String nome,
        String email,
        @NotBlank(message = "A senha não pode ficar em branco")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String senha,
        @NotBlank(message = "O CRECI não pode ficar em branco")
        String creci,
        String whatsapp
) {
    // Formatos são validados pelos próprios tipos de valor, com as mesmas mensagens em toda parte
    public Corretor.Dados toDados() {
        FieldErrors errors = new FieldErrors();
        Email emailValido = errors.collect(() -> new Email(email));
        Telefone whatsappValido = errors.collect(() -> whatsapp(whatsapp));
        errors.throwIfAny();
        return new Corretor.Dados(nome, emailValido, creci, whatsappValido);
    }

    // O WhatsApp é um Telefone, mas o erro precisa voltar no campo do JSON
    private static Telefone whatsapp(String valor) {
        try {
            return new Telefone(valor);
        } catch (ValorInvalidoException e) {
            throw new ValorInvalidoException("whatsapp", e.getMessage());
        }
    }
}
