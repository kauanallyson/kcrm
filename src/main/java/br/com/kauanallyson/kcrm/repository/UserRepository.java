package br.com.kauanallyson.kcrm.repository;

import br.com.kauanallyson.kcrm.model.Cpf;
import br.com.kauanallyson.kcrm.model.Email;
import br.com.kauanallyson.kcrm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmailOrCpf(Email email, Cpf cpf);

    @Query("select count(u) > 0 from User u where (u.email = :email or u.cpf = :cpf) and u.id <> :id")
    boolean existsByEmailOrCpfAndIdNot(Email email, Cpf cpf, UUID id);

    Optional<User> findByEmail(Email email);
}
