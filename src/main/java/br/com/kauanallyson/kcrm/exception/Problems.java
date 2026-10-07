package br.com.kauanallyson.kcrm.exception;

import org.springframework.http.ProblemDetail;

public final class Problems {
    private Problems() {
    }

    public static ProblemDetail of(ErrorCode code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(code.status(), detail);
        problem.setProperty("code", code.name());
        return problem;
    }

    public static ProblemDetail unauthenticated() {
        return of(ErrorCode.NAO_AUTENTICADO, "Token ausente ou inválido");
    }

    public static ProblemDetail accessDenied() {
        return of(ErrorCode.ACESSO_NEGADO, "Acesso negado");
    }
}
