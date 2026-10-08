package br.com.kauanallyson.kcrm.model.common;

import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;

import java.util.Locale;
import java.util.regex.Pattern;

// Checagem de formato propositalmente simples: um @, parte local e domínio com ponto, sem espaços
public record Email(String value) {
    private static final int MAXIMO = 254;
    private static final Pattern FORMATO = Pattern.compile("^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$");

    public Email {
        value = Strings.requireText(value, "email", "O e-mail não pode ficar em branco").toLowerCase(Locale.ROOT);
        if (value.length() > MAXIMO) {
            throw new ValorInvalidoException("email", "O e-mail deve ter no máximo " + MAXIMO + " caracteres");
        }
        if (!FORMATO.matcher(value).matches()) {
            throw new ValorInvalidoException("email", "E-mail inválido: informe no formato nome@dominio.com");
        }
    }
}
