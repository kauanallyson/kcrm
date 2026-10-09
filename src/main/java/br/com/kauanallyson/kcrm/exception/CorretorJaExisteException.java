package br.com.kauanallyson.kcrm.exception;

public class CorretorJaExisteException extends DomainException {
    public CorretorJaExisteException() {
        super(ErrorCode.CORRETOR_JA_EXISTE, "E-mail já cadastrado");
    }
}
