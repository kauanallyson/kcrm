package br.com.kauanallyson.kcrm.repository;

import br.com.kauanallyson.kcrm.model.cliente.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    List<Cliente> findAllByCorretorIdOrderByNomeAsc(UUID corretorId);

    Optional<Cliente> findByIdAndCorretorId(UUID id, UUID corretorId);
}
