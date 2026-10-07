package br.com.kauanallyson.kcrm.exception;

public class MotivoObrigatorioException extends DomainException {
    public MotivoObrigatorioException() {
        super(ErrorCode.MOTIVO_OBRIGATORIO, "Informe o motivo da desativação");
    }
}
