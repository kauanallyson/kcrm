package br.com.kauanallyson.kcrm.exception;

import java.util.UUID;

public class ClienteNaoEncontradoException extends DomainException {
    public ClienteNaoEncontradoException(UUID id) {
        super(ErrorCode.CLIENTE_NAO_ENCONTRADO, "Cliente não encontrado: " + id);
    }
}
