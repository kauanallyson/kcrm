package br.com.kauanallyson.kcrm.exception;

import java.util.UUID;

// Vendido é definitivo: o Imóvel não é mais editado nem volta a Disponível
public class ImovelVendidoException extends DomainException {
    public ImovelVendidoException(UUID id) {
        super(ErrorCode.IMOVEL_VENDIDO, "O Imóvel já foi vendido e não pode mais ser alterado: " + id);
    }
}
