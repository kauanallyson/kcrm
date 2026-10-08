package br.com.kauanallyson.kcrm.auth;

import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.usuario.Usuario;
import br.com.kauanallyson.kcrm.repository.UsuarioRepository;
import lombok.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticatedUserService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public AuthenticatedUserService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        Email valido;
        try {
            valido = new Email(email);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }
        return usuarioRepository.findByEmail(valido)
                .map(AuthenticatedUser::from)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    public Optional<AuthenticatedUser> loadById(UUID id) {
        // Recarregado a cada requisição: um token de Usuário desativado deixa de valer na hora
        return usuarioRepository.findById(id)
                .filter(Usuario::isAtivo)
                .map(AuthenticatedUser::from);
    }
}
