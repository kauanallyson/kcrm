package br.com.kauanallyson.kcrm.dto;

import jakarta.validation.constraints.NotBlank;

public record DesativacaoRequest(
        @NotBlank
        String motivo
) {
}
