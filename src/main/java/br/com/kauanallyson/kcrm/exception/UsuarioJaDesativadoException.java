package br.com.kauanallyson.kcrm.exception;

import java.util.UUID;

public class UsuarioJaDesativadoException extends DomainException {
    public UsuarioJaDesativadoException(UUID id) {
        super(ErrorCode.USUARIO_JA_DESATIVADO, "Usuário já está desativado: " + id);
    }
}
