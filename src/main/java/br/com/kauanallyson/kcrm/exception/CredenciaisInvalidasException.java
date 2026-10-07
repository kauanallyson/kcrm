package br.com.kauanallyson.kcrm.exception;

public class CredenciaisInvalidasException extends DomainException {
    public CredenciaisInvalidasException() {
        super(ErrorCode.CREDENCIAIS_INVALIDAS, "E-mail ou senha inválidos");
    }
}
