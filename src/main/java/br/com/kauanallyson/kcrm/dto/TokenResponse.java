package br.com.kauanallyson.kcrm.dto;

public record TokenResponse(
        String token,
        String type,
        long expiresIn
) {
}
