package br.com.kauanallyson.kcrm.dto.administracao;

import br.com.kauanallyson.kcrm.model.corretor.Corretor;

import java.time.OffsetDateTime;
import java.util.UUID;

// O que o Administrador vê de um Corretor: nada de senha nem da Carteira
public record CorretorAdministradoResponse(
        UUID id,
        String nome,
        String email,
        String creci,
        OffsetDateTime cadastradoEm,
        boolean suspenso
) {
    public static CorretorAdministradoResponse from(Corretor corretor) {
        return new CorretorAdministradoResponse(corretor.getId(), corretor.getNome(), corretor.getEmail().value(),
                corretor.getCreci(), corretor.getCriadoEm(), corretor.isSuspenso());
    }
}
