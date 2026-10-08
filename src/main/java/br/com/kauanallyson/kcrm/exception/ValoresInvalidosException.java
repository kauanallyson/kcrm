package br.com.kauanallyson.kcrm.exception;

import java.util.Map;

// Reúne os erros de vários tipos de valor de uma requisição, para voltarem todos de uma vez, por campo
public class ValoresInvalidosException extends RuntimeException {
    private final Map<String, String> errors;

    public ValoresInvalidosException(Map<String, String> errors) {
        super("Falha na validação");
        this.errors = Map.copyOf(errors);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
