package br.com.kauanallyson.kcrm.dto.corretor;

import br.com.kauanallyson.kcrm.model.corretor.Corretor;

import java.util.UUID;

public record CorretorResponse(UUID id, String nome, String email, String creci, String whatsapp) {
    public static CorretorResponse from(Corretor corretor) {
        return new CorretorResponse(corretor.getId(), corretor.getNome(), corretor.getEmail().value(),
                corretor.getCreci(), corretor.getWhatsapp().value());
    }
}
