package br.com.kauanallyson.kcrm.exception;

public class InvalidCredentialsException extends DomainException {
    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS, "Invalid e-mail or password");
    }
}
