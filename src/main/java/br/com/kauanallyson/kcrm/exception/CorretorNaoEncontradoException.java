package br.com.kauanallyson.kcrm.exception;

import java.util.UUID;

public class CorretorNaoEncontradoException extends DomainException {
    public CorretorNaoEncontradoException(UUID id) {
        super(ErrorCode.CORRETOR_NAO_ENCONTRADO, "Corretor não encontrado: " + id);
    }
}
