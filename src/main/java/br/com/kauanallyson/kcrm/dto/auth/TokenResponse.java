package br.com.kauanallyson.kcrm.dto.auth;

public record TokenResponse(
        String token,
        String type,
        long expiresIn
) {
}
