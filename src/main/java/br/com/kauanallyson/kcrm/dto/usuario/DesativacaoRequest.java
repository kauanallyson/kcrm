package br.com.kauanallyson.kcrm.dto.usuario;

import jakarta.validation.constraints.NotBlank;

public record DesativacaoRequest(
        @NotBlank
        String motivo
) {
}
