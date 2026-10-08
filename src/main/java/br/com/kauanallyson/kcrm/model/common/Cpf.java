package br.com.kauanallyson.kcrm.model.common;

import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;

// A forma canônica, no banco e na API, é 000.000.000-00
public record Cpf(String value) {
    private static final int LENGTH = 11;
    private static final String MENSAGEM = "CPF inválido: informe 11 dígitos, no formato 000.000.000-00";

    public Cpf {
        String texto = Strings.requireText(value, "cpf", "O CPF não pode ficar em branco");
        String digits = Strings.requireDigits(texto, ".-", "cpf", MENSAGEM);
        if (!isValid(digits)) {
            throw new ValorInvalidoException("cpf", MENSAGEM);
        }
        value = digits.substring(0, 3) + "." + digits.substring(3, 6) + "." + digits.substring(6, 9)
                + "-" + digits.substring(9);
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
