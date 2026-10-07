package br.com.kauanallyson.kcrm.dto;

import br.com.kauanallyson.kcrm.model.User;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String cpf,
        String email,
        String phone,
        String address
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getCpf().value(),
                user.getEmail().value(), user.getPhone(), user.getAddress());
    }
}
