package br.com.kauanallyson.kcrm.dto.historico;

import br.com.kauanallyson.kcrm.model.historico.Evento;
import br.com.kauanallyson.kcrm.model.historico.TipoEvento;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EventoResponse(
        TipoEvento tipo,
        String motivo,
        String detalhe,
        UUID autorId,
        String autorNome,
        OffsetDateTime ocorridoEm
) {
    public static EventoResponse from(Evento evento) {
        return new EventoResponse(evento.getTipo(), evento.getMotivo(), evento.getDetalhe(),
                evento.getAutor().getId(), evento.getAutor().getNome(), evento.getOcorridoEm());
    }
}
