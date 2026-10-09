package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

// sort só por campos liberados (nada de CPF nem corretor.*); o id no fim desempata, senão empates
// como nomes iguais caem em ordem arbitrária e a mesma linha aparece em duas páginas
final class Ordenacao {
    private Ordenacao() {
    }

    static Pageable restrita(Pageable pageable, Set<String> permitidos) {
        for (Sort.Order ordem : pageable.getSort()) {
            if (!permitidos.contains(ordem.getProperty())) {
                throw new ValorInvalidoException("sort", "Campo de ordenação inválido: " + ordem.getProperty());
            }
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().and(Sort.by("id")));
    }
}
