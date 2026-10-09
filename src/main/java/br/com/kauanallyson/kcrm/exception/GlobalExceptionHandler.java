package br.com.kauanallyson.kcrm.exception;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public final class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static ProblemDetail validacaoFalhou(Map<String, String> errors) {
        ProblemDetail problem = Problems.of(ErrorCode.VALIDACAO_FALHOU, "Falha na validação");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex) {
        return Problems.of(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(ValoresInvalidosException.class)
    public ProblemDetail handleValoresInvalidos(ValoresInvalidosException ex) {
        return validacaoFalhou(ex.getErrors());
    }

    // Um tipo de valor construído fora de FieldErrors ainda vira 400 por campo, nunca 500
    @ExceptionHandler(ValorInvalidoException.class)
    public ProblemDetail handleValorInvalido(ValorInvalidoException ex) {
        return validacaoFalhou(Map.of(ex.getCampo(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return Problems.of(ErrorCode.PARAMETRO_INVALIDO,
                "Valor inválido para '" + ex.getName() + "': " + ex.getValue());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        return Problems.of(ErrorCode.CONFLITO_DE_DADOS, "Os dados conflitam com um registro existente");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return Problems.of(ErrorCode.ERRO_INTERNO, "Erro inesperado");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        e -> String.valueOf(e.getDefaultMessage()),
                        (first, second) -> first));

        return ResponseEntity.badRequest().headers(headers).body(validacaoFalhou(errors));
    }
}
