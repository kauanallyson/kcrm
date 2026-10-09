package br.com.kauanallyson.kcrm.exception;

// Como na Suspensão: só chega a quem informou a senha certa
public class EmailNaoConfirmadoException extends DomainException {
    public EmailNaoConfirmadoException() {
        super(ErrorCode.EMAIL_NAO_CONFIRMADO, "E-mail não confirmado");
    }
}
