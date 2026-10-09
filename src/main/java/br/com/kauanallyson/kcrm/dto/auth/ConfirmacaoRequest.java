package br.com.kauanallyson.kcrm.dto.auth;

import jakarta.validation.constraints.NotBlank;

// O token que veio no link de Confirmação de E-mail e a senha do cadastro
public record ConfirmacaoRequest(
        @NotBlank(message = "O token não pode ficar em branco")
        String token,
        @NotBlank(message = "A senha não pode ficar em branco")
        String senha
) {
}
