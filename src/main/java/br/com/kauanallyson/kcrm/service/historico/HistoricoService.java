package br.com.kauanallyson.kcrm.service.historico;

import br.com.kauanallyson.kcrm.model.historico.Evento;
import br.com.kauanallyson.kcrm.model.historico.TipoEvento;
import br.com.kauanallyson.kcrm.model.historico.TipoRegistro;
import br.com.kauanallyson.kcrm.model.usuario.Usuario;
import br.com.kauanallyson.kcrm.repository.EventoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// Ponto único de escrita e leitura do Histórico, reutilizado por todas as features
@Service
public class HistoricoService {
    private final EventoRepository eventoRepository;

    public HistoricoService(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    // Exige a transação de quem chama: o Evento só existe se o fato que ele registra for gravado
    @Transactional(propagation = Propagation.MANDATORY)
    public Evento registrar(TipoEvento tipo, TipoRegistro registroTipo, UUID registroId,
                            Usuario autor, String motivo, String detalhe) {
        return eventoRepository.save(Evento.registrar(tipo, registroTipo, registroId, autor, motivo, detalhe));
    }

    // Mais recentes primeiro
    @Transactional(readOnly = true)
    public List<Evento> eventosDo(TipoRegistro registroTipo, UUID registroId) {
        return eventoRepository.findDoRegistro(registroTipo, registroId);
    }
}
