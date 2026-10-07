package br.com.kauanallyson.kcrm.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends DomainException {
    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid e-mail or password");
    }
}
