package br.com.kauanallyson.kcrm.repository;

import br.com.kauanallyson.kcrm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmailOrCpf(String email, String cpf);

    @Query("select count(u) > 0 from User u where (u.email = :email or u.cpf = :cpf) and u.id <> :id")
    boolean existsByEmailOrCpfAndIdNot(String email, String cpf, UUID id);

    Optional<User> findByEmail(String email);
}
