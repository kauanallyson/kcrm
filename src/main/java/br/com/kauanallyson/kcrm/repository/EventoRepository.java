package br.com.kauanallyson.kcrm.repository;

import br.com.kauanallyson.kcrm.model.historico.Evento;
import br.com.kauanallyson.kcrm.model.historico.TipoRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventoRepository extends JpaRepository<Evento, UUID> {
    @Query("""
            select e from Evento e join fetch e.autor
            where e.registroTipo = :registroTipo and e.registroId = :registroId
            order by e.ocorridoEm desc, e.id desc""")
    List<Evento> findDoRegistro(TipoRegistro registroTipo, UUID registroId);
}
