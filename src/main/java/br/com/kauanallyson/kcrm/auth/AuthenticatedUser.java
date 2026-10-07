package br.com.kauanallyson.kcrm.auth;

import br.com.kauanallyson.kcrm.model.usuario.Perfil;
import br.com.kauanallyson.kcrm.model.usuario.Usuario;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record AuthenticatedUser(UUID id, String email, String senhaHash, Perfil perfil, boolean ativo) implements UserDetails {

    public static AuthenticatedUser from(Usuario usuario) {
        return new AuthenticatedUser(usuario.getId(), usuario.getEmail().value(), usuario.getSenhaHash().value(),
                usuario.getPerfil(), usuario.isAtivo());
    }

    public UUID getId() {
        return id;
    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    // Um Usuário desativado não passa no login (DaoAuthenticationProvider checa isEnabled)
    @Override
    public boolean isEnabled() {
        return ativo;
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
