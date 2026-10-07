package br.com.kauanallyson.kcrm.repository;

import br.com.kauanallyson.kcrm.model.Cpf;
import br.com.kauanallyson.kcrm.model.Email;
import br.com.kauanallyson.kcrm.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    boolean existsByEmailOrCpf(Email email, Cpf cpf);

    @Query("select count(u) > 0 from Usuario u where (u.email = :email or u.cpf = :cpf) and u.id <> :id")
    boolean existsByEmailOrCpfAndIdNot(Email email, Cpf cpf, UUID id);

    Optional<Usuario> findByEmail(Email email);
}
