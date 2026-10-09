package br.com.kauanallyson.kcrm.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

// Formato estável de página na API, independente da serialização do PageImpl
public record PaginaResponse<T>(
        List<T> conteudo,
        int pagina,
        int tamanho,
        long totalElementos,
        int totalPaginas
) {
    public static <E, T> PaginaResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return new PaginaResponse<>(page.getContent().stream().map(mapper).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
