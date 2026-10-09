package br.com.kauanallyson.kcrm.auth;

import br.com.kauanallyson.kcrm.model.administrador.Administrador;
import br.com.kauanallyson.kcrm.model.corretor.Corretor;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

// Credenciais usadas só no login; depois dele, o domínio recebe apenas o CorretorId ou o AdministradorId.
// Suspensão e e-mail não confirmado não bloqueiam aqui: o login só os revela depois de conferir a senha
public record AuthenticatedUser(
        UUID id,
        String email,
        String senhaHash,
        Papel papel,
        boolean suspenso,
        boolean emailConfirmado
) implements UserDetails {

    public static AuthenticatedUser from(Corretor corretor) {
        return new AuthenticatedUser(corretor.getId(), corretor.getEmail().value(), corretor.getSenhaHash().value(),
                Papel.CORRETOR, corretor.isSuspenso(), corretor.isEmailConfirmado());
    }

    public static AuthenticatedUser from(Administrador administrador) {
        return new AuthenticatedUser(administrador.getId(), administrador.getEmail().value(),
                administrador.getSenhaHash().value(), Papel.ADMINISTRADOR, false, true);
    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(papel.authority());
    }

    @Override
    public String getPassword() {
        return senhaHash;
    }

    @Override
    @NonNull
    public String getUsername() {
        return email;
    }
}
