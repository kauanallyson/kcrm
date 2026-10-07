package br.com.kauanallyson.kcrm.auth;

import br.com.kauanallyson.kcrm.model.Usuario;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record AuthenticatedUser(UUID id, String email, String senhaHash) implements UserDetails {

    public static AuthenticatedUser from(Usuario usuario) {
        return new AuthenticatedUser(usuario.getId(), usuario.getEmail().value(), usuario.getSenhaHash().value());
    }

    public UUID getId() {
        return id;
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
