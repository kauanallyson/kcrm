package br.com.kauanallyson.kcrm.exception;

import java.util.UUID;

public class ImovelNaoEncontradoException extends DomainException {
    public ImovelNaoEncontradoException(UUID id) {
        super(ErrorCode.IMOVEL_NAO_ENCONTRADO, "Imóvel não encontrado: " + id);
    }
}
