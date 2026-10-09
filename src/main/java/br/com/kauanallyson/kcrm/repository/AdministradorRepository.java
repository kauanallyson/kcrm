package br.com.kauanallyson.kcrm.repository;

import br.com.kauanallyson.kcrm.model.administrador.Administrador;
import br.com.kauanallyson.kcrm.model.common.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, UUID> {
    boolean existsByEmail(Email email);

    Optional<Administrador> findByEmail(Email email);
}
