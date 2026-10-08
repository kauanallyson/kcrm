package br.com.kauanallyson.kcrm.dto.cliente;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TransferenciaRequest(
        @NotNull(message = "Informe o Corretor que passa a atender o Cliente")
        UUID corretorId,
        String motivo
) {
}
