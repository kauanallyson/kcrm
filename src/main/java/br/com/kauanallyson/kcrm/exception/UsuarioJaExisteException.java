package br.com.kauanallyson.kcrm.exception;

public class UsuarioJaExisteException extends DomainException {
    public UsuarioJaExisteException() {
        super(ErrorCode.USUARIO_JA_EXISTE, "E-mail ou CPF já cadastrado");
    }
}
