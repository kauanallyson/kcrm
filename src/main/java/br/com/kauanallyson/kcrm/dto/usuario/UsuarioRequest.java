package br.com.kauanallyson.kcrm.dto.usuario;

import br.com.kauanallyson.kcrm.dto.common.EnderecoRequest;
import br.com.kauanallyson.kcrm.dto.common.FieldErrors;
import br.com.kauanallyson.kcrm.model.common.Cpf;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.common.Endereco;
import br.com.kauanallyson.kcrm.model.common.Telefone;
import br.com.kauanallyson.kcrm.model.usuario.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "O nome não pode ficar em branco")
        String nome,
        String cpf,
        String email,
        @NotBlank(message = "A senha não pode ficar em branco")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String senha,
        String telefone,
        @NotNull(message = "O endereço não pode ficar em branco")
        EnderecoRequest endereco
) {
    // Formatos são validados pelos próprios tipos de valor, com as mesmas mensagens em toda parte
    public Usuario.Dados toDados() {
        FieldErrors errors = new FieldErrors();
        Cpf cpfValido = errors.collect(() -> new Cpf(cpf));
        Email emailValido = errors.collect(() -> new Email(email));
        Telefone telefoneValido = errors.collect(() -> new Telefone(telefone));
        Endereco enderecoValido = endereco.toEndereco(errors);
        errors.throwIfAny();
        return new Usuario.Dados(nome.strip(), cpfValido, emailValido, telefoneValido, enderecoValido);
    }
}
