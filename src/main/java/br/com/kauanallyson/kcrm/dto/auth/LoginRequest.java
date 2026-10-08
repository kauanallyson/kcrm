package br.com.kauanallyson.kcrm.dto.auth;

import jakarta.validation.constraints.NotBlank;

// Sem checar o formato do e-mail: um e-mail malformado simplesmente não confere
public record LoginRequest(
        @NotBlank(message = "O e-mail não pode ficar em branco")
        String email,
        @NotBlank(message = "A senha não pode ficar em branco")
        String senha
) {
}
