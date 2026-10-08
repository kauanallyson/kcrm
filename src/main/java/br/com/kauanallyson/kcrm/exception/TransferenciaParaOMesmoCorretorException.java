package br.com.kauanallyson.kcrm.exception;

public class TransferenciaParaOMesmoCorretorException extends DomainException {
    public TransferenciaParaOMesmoCorretorException() {
        super(ErrorCode.TRANSFERENCIA_PARA_O_MESMO_CORRETOR, "O Cliente já é atendido por esse Corretor");
    }
}
