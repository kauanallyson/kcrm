package br.com.kauanallyson.kcrm.repository;

import br.com.kauanallyson.kcrm.model.common.Cpf;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.usuario.Perfil;
import br.com.kauanallyson.kcrm.model.usuario.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    boolean existsByEmailOrCpf(Email email, Cpf cpf);

    @Query("select count(u) > 0 from Usuario u where (u.email = :email or u.cpf = :cpf) and u.id <> :id")
    boolean existsByEmailOrCpfAndIdNot(Email email, Cpf cpf, UUID id);

    Optional<Usuario> findByEmail(Email email);

    boolean existsByPerfilAndAtivoTrue(Perfil perfil);

    // Trava as linhas dos Admins ativos para que duas operações simultâneas não removam o último Admin
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.perfil = br.com.kauanallyson.kcrm.model.usuario.Perfil.ADMIN and u.ativo = true")
    List<Usuario> travarAdminsAtivos();

    List<Usuario> findAllByOrderByNomeAsc();

    List<Usuario> findAllByPerfilAndAtivoTrue(Perfil perfil);
}
