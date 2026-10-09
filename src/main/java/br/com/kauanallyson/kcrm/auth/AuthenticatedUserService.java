package br.com.kauanallyson.kcrm.auth;

import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.corretor.Corretor;
import br.com.kauanallyson.kcrm.repository.CorretorRepository;
import lombok.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticatedUserService implements UserDetailsService {

    private final CorretorRepository corretorRepository;

    public AuthenticatedUserService(CorretorRepository corretorRepository) {
        this.corretorRepository = corretorRepository;
    }

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        Email valido;
        try {
            valido = new Email(email);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("Corretor não encontrado");
        }
        return corretorRepository.findByEmail(valido)
                .map(AuthenticatedUser::from)
                .orElseThrow(() -> new UsernameNotFoundException("Corretor não encontrado"));
    }

    public Optional<AuthenticatedUser> loadById(UUID id) {
        // Recarregado a cada requisição: o token de um Corretor que não existe mais deixa de valer
        return corretorRepository.findById(id)
                .map(AuthenticatedUser::from);
    }
}
