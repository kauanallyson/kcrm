package br.com.kauanallyson.kcrm.dto.auth;

import jakarta.validation.constraints.NotBlank;

// Sem checar o formato: um e-mail malformado simplesmente não recebe nada
public record ReenvioConfirmacaoRequest(
        @NotBlank(message = "O e-mail não pode ficar em branco")
        String email
) {
}
