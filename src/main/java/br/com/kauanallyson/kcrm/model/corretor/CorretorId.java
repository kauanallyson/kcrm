package br.com.kauanallyson.kcrm.model.corretor;

import java.util.UUID;

// Identidade do Corretor autenticado: é tudo o que o domínio recebe da autenticação
public record CorretorId(UUID value) {
    public CorretorId {
        if (value == null) {
            throw new IllegalArgumentException("O id do Corretor não pode ser nulo");
        }
    }
}
