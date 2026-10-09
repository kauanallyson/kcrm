package br.com.kauanallyson.kcrm.exception;

// 403, não 401: o token continua válido, só a confirmação da senha falhou
public class SenhaIncorretaException extends DomainException {
    public SenhaIncorretaException() {
        super(ErrorCode.SENHA_INCORRETA, "Senha incorreta");
    }
}
