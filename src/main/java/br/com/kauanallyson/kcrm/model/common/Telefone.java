package br.com.kauanallyson.kcrm.model.common;

import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;

import java.util.Set;

// Celular com DDD; a forma canônica, no banco e na API, é (xx) xxxxx-xxxx
public record Telefone(String value) {
    private static final int LENGTH = 11;
    private static final String MENSAGEM =
            "Número inválido: informe DDD e celular com 9 dígitos, no formato (xx) xxxxx-xxxx";
    private static final Set<Integer> DDDS = Set.of(
            11, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 24, 27, 28, 31, 32, 33, 34, 35, 37, 38,
            41, 42, 43, 44, 45, 46, 47, 48, 49, 51, 53, 54, 55, 61, 62, 63, 64, 65, 66, 67, 68, 69,
            71, 73, 74, 75, 77, 79, 81, 82, 83, 84, 85, 86, 87, 88, 89, 91, 92, 93, 94, 95, 96, 97, 98, 99);

    public Telefone {
        String texto = Strings.requireText(value, "telefone", "O número não pode ficar em branco");
        String digits = Strings.requireDigits(texto, " ()-", "telefone", MENSAGEM);
        if (digits.length() != LENGTH) {
            throw new ValorInvalidoException("telefone", MENSAGEM);
        }
        if (!DDDS.contains(Integer.parseInt(digits.substring(0, 2)))) {
            throw new ValorInvalidoException("telefone", "Número inválido: DDD " + digits.substring(0, 2) + " não existe");
        }
        if (digits.charAt(2) != '9') {
            throw new ValorInvalidoException("telefone", "Número inválido: o celular deve começar com 9");
        }
        value = "(" + digits.substring(0, 2) + ") " + digits.substring(2, 7) + "-" + digits.substring(7);
    }
}
