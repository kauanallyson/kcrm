package br.com.kauanallyson.kcrm.model.common;

import java.util.Locale;
import java.util.Objects;

// As 27 unidades federativas do Brasil, pela sigla
public enum Uf {
    AC, AL, AP, AM, BA, CE, DF, ES, GO, MA, MT, MS, MG, PA, PB, PR, PE, PI, RJ, RN, RS, RO, RR, SC, SP, SE, TO;

    public static Uf daSigla(String sigla) {
        Objects.requireNonNull(sigla, "estado");
        String normalizada = sigla.strip().toUpperCase(Locale.ROOT);
        for (Uf uf : values()) {
            if (uf.name().equals(normalizada)) {
                return uf;
            }
        }
        throw new IllegalArgumentException("Estado inválido: informe a sigla de uma UF, como CE ou SP");
    }
}
