package br.com.kauanallyson.kcrm.repository;

import br.com.kauanallyson.kcrm.model.cliente.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    Page<Cliente> findAllByCorretorId(UUID corretorId, Pageable pageable);

    Optional<Cliente> findByIdAndCorretorId(UUID id, UUID corretorId);
}
