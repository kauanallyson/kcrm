package br.com.kauanallyson.kcrm.auth;

import br.com.kauanallyson.kcrm.model.corretor.Corretor;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

// Credenciais do Corretor usadas só no login; depois dele, o domínio recebe apenas o CorretorId
public record AuthenticatedUser(UUID id, String email, String senhaHash) implements UserDetails {

    public static AuthenticatedUser from(Corretor corretor) {
        return new AuthenticatedUser(corretor.getId(), corretor.getEmail().value(), corretor.getSenhaHash().value());
    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
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
