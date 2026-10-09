package br.com.kauanallyson.kcrm.exception;

import java.util.UUID;

public class UsuarioNaoEncontradoException extends DomainException {
    public UsuarioNaoEncontradoException(UUID id) {
        super(ErrorCode.USUARIO_NAO_ENCONTRADO, "Usuário não encontrado: " + id);
    }
}
