package br.com.kauanallyson.kcrm.model.administrador;

import java.util.UUID;

// Identidade do Administrador autenticado; nunca serve de caminho até uma Carteira
public record AdministradorId(UUID value) {
    public AdministradorId {
        if (value == null) {
            throw new IllegalArgumentException("O id do Administrador não pode ser nulo");
        }
    }
}
