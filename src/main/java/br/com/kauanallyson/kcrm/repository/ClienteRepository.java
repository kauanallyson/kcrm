package br.com.kauanallyson.kcrm.repository;

import br.com.kauanallyson.kcrm.model.cliente.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    Page<Cliente> findAllByCorretorId(UUID corretorId, Pageable pageable);

    List<Cliente> findAllByCorretorId(UUID corretorId, Sort sort);

    Optional<Cliente> findByIdAndCorretorId(UUID id, UUID corretorId);

    @Modifying
    @Query("delete from Cliente x where x.corretor.id = :corretorId")
    int apagarTodosDoCorretor(@Param("corretorId") UUID corretorId);
}
