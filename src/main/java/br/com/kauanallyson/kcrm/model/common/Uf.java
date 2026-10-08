package br.com.kauanallyson.kcrm.model.common;

import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;

import java.util.Locale;

// As 27 unidades federativas do Brasil, pela sigla
public enum Uf {
    AC, AL, AP, AM, BA, CE, DF, ES, GO, MA, MT, MS, MG, PA, PB, PR, PE, PI, RJ, RN, RS, RO, RR, SC, SP, SE, TO;

    public static Uf daSigla(String sigla) {
        String normalizada = Strings.requireText(sigla, "endereco.estado", "O estado não pode ficar em branco").toUpperCase(Locale.ROOT);
        for (Uf uf : values()) {
            if (uf.name().equals(normalizada)) {
                return uf;
            }
        }
        throw new ValorInvalidoException("endereco.estado", "Estado inválido: informe a sigla de uma UF, como CE ou SP");
    }
}
