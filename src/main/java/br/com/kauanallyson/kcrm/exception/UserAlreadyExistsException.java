package br.com.kauanallyson.kcrm.exception;

public class UserAlreadyExistsException extends DomainException {
    public UserAlreadyExistsException() {
        super(ErrorCode.USER_ALREADY_EXISTS, "E-mail or CPF already in use");
    }
}
