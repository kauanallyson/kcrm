package br.com.kauanallyson.kcrm.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException(UUID id) {
        super(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found: " + id);
    }
}
