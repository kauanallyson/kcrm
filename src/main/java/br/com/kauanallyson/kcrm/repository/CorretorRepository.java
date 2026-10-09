package br.com.kauanallyson.kcrm.repository;

import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.corretor.Corretor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CorretorRepository extends JpaRepository<Corretor, UUID> {
    boolean existsByEmail(Email email);

    Optional<Corretor> findByEmail(Email email);
}
