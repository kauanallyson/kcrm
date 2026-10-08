package br.com.kauanallyson.kcrm.dto.common;

import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;
import br.com.kauanallyson.kcrm.exception.ValoresInvalidosException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

// Builds several value types, collecting every ValorInvalidoException instead of stopping at the first
public final class FieldErrors {
    private final Map<String, String> errors = new LinkedHashMap<>();

    public <T> T collect(Supplier<T> factory) {
        try {
            return factory.get();
        } catch (ValorInvalidoException e) {
            errors.putIfAbsent(e.getCampo(), e.getMessage());
            return null;
        }
    }

    public void throwIfAny() {
        if (!errors.isEmpty()) {
            throw new ValoresInvalidosException(errors);
        }
    }
}
