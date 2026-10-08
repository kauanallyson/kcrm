package br.com.kauanallyson.kcrm.model.common;

import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;

// A forma canônica, no banco e na API, é 00000-000
public record Cep(String value) {
    private static final int LENGTH = 8;
    private static final String MENSAGEM = "CEP inválido: informe 8 dígitos, no formato 00000-000";

    public Cep {
        String texto = Strings.requireText(value, "endereco.cep", "O CEP não pode ficar em branco");
        String digits = Strings.requireDigits(texto, "-", "endereco.cep", MENSAGEM);
        if (digits.length() != LENGTH || digits.chars().distinct().count() == 1) {
            throw new ValorInvalidoException("endereco.cep", MENSAGEM);
        }
        value = digits.substring(0, 5) + "-" + digits.substring(5);
    }
}
