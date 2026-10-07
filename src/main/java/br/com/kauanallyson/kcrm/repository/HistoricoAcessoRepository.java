package br.com.kauanallyson.kcrm.repository;

import br.com.kauanallyson.kcrm.model.usuario.HistoricoAcesso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistoricoAcessoRepository extends JpaRepository<HistoricoAcesso, UUID> {
    @Query("select h from HistoricoAcesso h join fetch h.admin where h.usuario.id = :usuarioId order by h.ocorridoEm, h.id")
    List<HistoricoAcesso> findByUsuarioId(UUID usuarioId);
}
