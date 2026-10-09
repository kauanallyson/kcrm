package br.com.kauanallyson.kcrm.exception;

// Só chega a quem provou a senha ou tem token válido; a quem não sabe a senha, o login responde o 401 genérico
public class ContaSuspensaException extends DomainException {
    public ContaSuspensaException() {
        super(ErrorCode.CONTA_SUSPENSA, "Conta suspensa");
    }
}
