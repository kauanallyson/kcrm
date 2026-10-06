package br.com.kauanallyson.kcrm.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String cpf,
        String email,
        String phone,
        String address
) {
}
