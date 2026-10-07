package br.com.kauanallyson.kcrm.exception;

import java.util.UUID;

public class UsuarioJaAtivoException extends DomainException {
    public UsuarioJaAtivoException(UUID id) {
        super(ErrorCode.USUARIO_JA_ATIVO, "Usuário já está ativo: " + id);
    }
}
