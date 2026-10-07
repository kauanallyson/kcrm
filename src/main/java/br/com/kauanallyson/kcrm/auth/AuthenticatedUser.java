package br.com.kauanallyson.kcrm.auth;

import br.com.kauanallyson.kcrm.model.User;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record AuthenticatedUser(UUID id, String email, String passwordHash) implements UserDetails {

    public static AuthenticatedUser from(User user) {
        return new AuthenticatedUser(user.getId(), user.getEmail().value(), user.getPasswordHash().value());
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
        return passwordHash;
    }

    @Override
    @NonNull
    public String getUsername() {
        return email;
    }
}
