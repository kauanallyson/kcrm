package br.com.kauanallyson.kcrm.model;

import java.util.Objects;

public record Cpf(String value) {
    private static final int LENGTH = 11;

    public Cpf {
        Objects.requireNonNull(value, "cpf");
        value = value.replaceAll("\\D", "");
        if (!isValid(value)) {
            throw new IllegalArgumentException("CPF inválido");
        }
    }

    private static boolean isValid(String digits) {
        if (digits.length() != LENGTH || digits.chars().distinct().count() == 1) {
            return false;
        }
        return checkDigit(digits, 9) == digits.charAt(9) - '0'
                && checkDigit(digits, 10) == digits.charAt(10) - '0';
    }

    private static int checkDigit(String digits, int position) {
        int sum = 0;
        for (int i = 0; i < position; i++) {
            sum += (digits.charAt(i) - '0') * (position + 1 - i);
        }
        int remainder = sum * 10 % 11;
        return remainder == 10 ? 0 : remainder;
    }
}
