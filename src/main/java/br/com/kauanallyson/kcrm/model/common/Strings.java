package br.com.kauanallyson.kcrm.model.common;

import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;

// Checks shared by the value types; per-field rules (length, letters...) stay inline in each type
final class Strings {
    private Strings() {
    }

    // Rejects null and blank; returns the value stripped
    static String requireText(
            String value,
            String field,
            String message
    ) {
        if (value == null || value.isBlank()) {
            throw new ValorInvalidoException(field, message);
        }
        return value.strip();
    }

    // Rejects any character that is neither a digit nor in allowedMask, instead of dropping it silently;
    // returns only the digits
    static String requireDigits(
            String value,
            String allowedMask,
            String field,
            String message
    ) {
        for (char c : value.toCharArray()) {
            if (!Character.isDigit(c) && allowedMask.indexOf(c) < 0) {
                throw new ValorInvalidoException(field, message);
            }
        }
        return value.replaceAll("\\D", "");
    }
}
