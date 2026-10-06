package br.com.kauanallyson.kcrm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record UserRequest(
        @NotBlank
        String name,
        @NotBlank
        @CPF
        String cpf,
        @NotBlank
        @Email
        String email,
        @NotBlank
        @Size(min = 8, message = "must be at least 8 characters")
        String password,
        @NotBlank
        String phone,
        @NotBlank
        String address
) {
}
