package br.com.kauanallyson.kcrm.exception;

public class UltimoAdminException extends DomainException {
    public UltimoAdminException() {
        super(ErrorCode.ULTIMO_ADMIN, "O último Admin ativo não pode ser desativado nem deixar de ser Admin");
    }
}
