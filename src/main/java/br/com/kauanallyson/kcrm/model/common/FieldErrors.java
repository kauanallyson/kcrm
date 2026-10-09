package br.com.kauanallyson.kcrm.model.common;

import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;
import br.com.kauanallyson.kcrm.exception.ValoresInvalidosException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

// Builds several value types, collecting every ValorInvalidoException instead of stopping at the first
public final class FieldErrors {
    private final Map<String, String> errors = new LinkedHashMap<>();

    public <T> T collect(Supplier<T> factory) {
        try {
            return factory.get();
        } catch (ValorInvalidoException e) {
            adicionar(e.getCampo(), e.getMessage());
            return null;
        } catch (ValoresInvalidosException e) {
            e.getErrors().forEach(this::adicionar);
            return null;
        }
    }

    // Os tipos de valor apontam o próprio campo; aqui o erro volta no campo do JSON onde o valor foi informado
    public <T> T collect(String campo, Supplier<T> factory) {
        try {
            return factory.get();
        } catch (ValorInvalidoException e) {
            adicionar(campo, e.getMessage());
            return null;
        }
    }

    // Campo obrigatório ausente (nulo ou texto em branco) vira erro do campo, junto com os demais
    public void exigir(
            String campo,
            Object valor,
            String mensagem
    ) {
        if (valor == null || valor instanceof String texto && texto.isBlank()) {
            adicionar(campo, mensagem);
        }
    }

    // Regra de negócio violada vira erro do campo, junto com os demais
    public void rejeitar(String campo, String mensagem) {
        adicionar(campo, mensagem);
    }

    public void throwIfAny() {
        if (!errors.isEmpty()) {
            throw new ValoresInvalidosException(errors);
        }
    }

    // O erro mais específico vence: com "endereco.cep" já apontado, "endereco" ausente não se repete
    private void adicionar(String campo, String mensagem) {
        boolean jaTemFilho = errors.keySet().stream().anyMatch(chave -> chave.startsWith(campo + "."));
        if (!jaTemFilho) {
            errors.putIfAbsent(campo, mensagem);
        }
    }
}
