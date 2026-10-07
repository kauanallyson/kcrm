package br.com.kauanallyson.kcrm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public final class Problems {
    private Problems() {
    }

    public static ProblemDetail of(HttpStatus status, String code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty("code", code);
        return problem;
    }

    public static ProblemDetail unauthenticated() {
        return of(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Missing or invalid token");
    }

    public static ProblemDetail accessDenied() {
        return of(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied");
    }
}
