package br.com.kauanallyson.kcrm.dto.usuario;

import br.com.kauanallyson.kcrm.model.common.Cpf;
import br.com.kauanallyson.kcrm.model.common.Telefone;
import br.com.kauanallyson.kcrm.model.usuario.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
        @Pattern(regexp = "^[^0-9]*([0-9][^0-9]*){11}$", message = "deve ter DDD e celular com 9 dígitos: (xx) xxxxx-xxxx")
        String telefone,
        @NotBlank
        String endereco
) {
    public Usuario.Dados toDados() {
        return new Usuario.Dados(nome, new Cpf(cpf), new br.com.kauanallyson.kcrm.model.common.Email(email), new Telefone(telefone), endereco);
    }
}
