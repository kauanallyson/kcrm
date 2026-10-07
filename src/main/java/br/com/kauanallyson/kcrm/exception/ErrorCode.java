package br.com.kauanallyson.kcrm.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    USUARIO_NAO_ENCONTRADO(HttpStatus.NOT_FOUND),
    USUARIO_JA_EXISTE(HttpStatus.CONFLICT),
    CREDENCIAIS_INVALIDAS(HttpStatus.UNAUTHORIZED),
    NAO_AUTENTICADO(HttpStatus.UNAUTHORIZED),
    ACESSO_NEGADO(HttpStatus.FORBIDDEN),
    PARAMETRO_INVALIDO(HttpStatus.BAD_REQUEST),
    VALIDACAO_FALHOU(HttpStatus.BAD_REQUEST),
    CONFLITO_DE_DADOS(HttpStatus.CONFLICT),
    ERRO_INTERNO(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
