package br.com.kauanallyson.kcrm.dto;

import br.com.kauanallyson.kcrm.model.Cpf;
import br.com.kauanallyson.kcrm.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record UsuarioRequest(
        @NotBlank
        String nome,
        @NotBlank
        @CPF
        String cpf,
        @NotBlank
        @Email
        String email,
        @NotBlank
        @Size(min = 8, message = "deve ter pelo menos 8 caracteres")
        String senha,
        @NotBlank
        String telefone,
        @NotBlank
        String endereco
) {
    public Usuario.Dados toDados() {
        return new Usuario.Dados(nome, new Cpf(cpf), new br.com.kauanallyson.kcrm.model.Email(email), telefone, endereco);
    }
}
