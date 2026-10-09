package br.com.kauanallyson.kcrm.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    CORRETOR_NAO_ENCONTRADO(HttpStatus.NOT_FOUND),
    CLIENTE_NAO_ENCONTRADO(HttpStatus.NOT_FOUND),
    IMOVEL_NAO_ENCONTRADO(HttpStatus.NOT_FOUND),
    CORRETOR_JA_EXISTE(HttpStatus.CONFLICT),
    IMOVEL_VENDIDO(HttpStatus.CONFLICT),
    CREDENCIAIS_INVALIDAS(HttpStatus.UNAUTHORIZED),
    NAO_AUTENTICADO(HttpStatus.UNAUTHORIZED),
    ACESSO_NEGADO(HttpStatus.FORBIDDEN),
    PARAMETRO_INVALIDO(HttpStatus.BAD_REQUEST),
    VALIDACAO_FALHOU(HttpStatus.BAD_REQUEST),
    CONFLITO_DE_DADOS(HttpStatus.CONFLICT),
    MUITAS_REQUISICOES(HttpStatus.TOO_MANY_REQUESTS),
    ERRO_INTERNO(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
