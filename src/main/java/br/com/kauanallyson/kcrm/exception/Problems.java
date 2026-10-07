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
        return of(ErrorCode.UNAUTHENTICATED, "Missing or invalid token");
    }

    public static ProblemDetail accessDenied() {
        return of(ErrorCode.ACCESS_DENIED, "Access denied");
    }
}
