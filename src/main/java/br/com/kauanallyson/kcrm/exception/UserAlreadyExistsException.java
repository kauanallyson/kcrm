package br.com.kauanallyson.kcrm.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends DomainException {
    public UserAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", "E-mail or CPF already in use");
    }
}
