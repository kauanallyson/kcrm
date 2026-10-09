package br.com.kauanallyson.kcrm.repository;

import br.com.kauanallyson.kcrm.model.imovel.Imovel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ImovelRepository extends JpaRepository<Imovel, UUID> {
    Page<Imovel> findAllByCorretorId(UUID corretorId, Pageable pageable);

    Optional<Imovel> findByIdAndCorretorId(UUID id, UUID corretorId);
}
