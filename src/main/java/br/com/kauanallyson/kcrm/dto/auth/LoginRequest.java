package br.com.kauanallyson.kcrm.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String senha
) {
}
