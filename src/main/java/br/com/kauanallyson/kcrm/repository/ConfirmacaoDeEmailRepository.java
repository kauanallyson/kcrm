package br.com.kauanallyson.kcrm.repository;

import br.com.kauanallyson.kcrm.model.corretor.ConfirmacaoDeEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfirmacaoDeEmailRepository extends JpaRepository<ConfirmacaoDeEmail, UUID> {
    Optional<ConfirmacaoDeEmail> findByTokenHash(String tokenHash);
}
