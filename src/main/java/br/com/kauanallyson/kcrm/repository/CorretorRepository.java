package br.com.kauanallyson.kcrm.repository;

import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.corretor.Corretor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CorretorRepository extends JpaRepository<Corretor, UUID> {
    boolean existsByEmail(Email email);

    Optional<Corretor> findByEmail(Email email);

    @Modifying
    @Query("delete from Corretor c where c.emailConfirmado = false and c.criadoEm < :limite")
    int apagarNaoConfirmadosCriadosAntesDe(@Param("limite") OffsetDateTime limite);
}
