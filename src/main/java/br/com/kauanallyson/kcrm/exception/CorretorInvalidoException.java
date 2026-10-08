package br.com.kauanallyson.kcrm.exception;

public class CorretorInvalidoException extends DomainException {
    public CorretorInvalidoException() {
        super(ErrorCode.CORRETOR_INVALIDO, "O Cliente só pode ser atendido por um Corretor ativo");
    }
}
