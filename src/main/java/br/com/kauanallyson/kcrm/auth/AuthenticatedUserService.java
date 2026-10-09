package br.com.kauanallyson.kcrm.auth;

import br.com.kauanallyson.kcrm.exception.ContaSuspensaException;
import br.com.kauanallyson.kcrm.exception.EmailNaoConfirmadoException;
import br.com.kauanallyson.kcrm.model.administrador.AdministradorId;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.corretor.Corretor;
import br.com.kauanallyson.kcrm.model.corretor.CorretorId;
import br.com.kauanallyson.kcrm.repository.AdministradorRepository;
import br.com.kauanallyson.kcrm.repository.CorretorRepository;
import lombok.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticatedUserService implements UserDetailsService {

    private final CorretorRepository corretorRepository;
    private final AdministradorRepository administradorRepository;

    public AuthenticatedUserService(
            CorretorRepository corretorRepository,
            AdministradorRepository administradorRepository
    ) {
        this.corretorRepository = corretorRepository;
        this.administradorRepository = administradorRepository;
    }

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        Email valido;
        try {
            valido = new Email(email);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("Não encontrado");
        }
        return administradorRepository.findByEmail(valido)
                .map(AuthenticatedUser::from)
                .or(() -> corretorRepository.findByEmail(valido).map(AuthenticatedUser::from))
                .orElseThrow(() -> new UsernameNotFoundException("Não encontrado"));
    }

    // Conferido a cada requisição: o token de quem não existe mais deixa de valer,
    // e o de um Corretor Suspenso responde 403 na hora
    public Optional<Object> principal(Sujeito sujeito) {
        return switch (sujeito.papel()) {
            case ADMINISTRADOR -> administradorRepository.existsById(sujeito.id())
                    ? Optional.of(new AdministradorId(sujeito.id()))
                    : Optional.empty();
            case CORRETOR -> corretorRepository.findById(sujeito.id()).map(AuthenticatedUserService::ativo);
        };
    }

    private static CorretorId ativo(Corretor corretor) {
        if (corretor.isSuspenso()) {
            throw new ContaSuspensaException();
        }
        if (!corretor.isEmailConfirmado()) {
            throw new EmailNaoConfirmadoException();
        }
        return new CorretorId(corretor.getId());
    }
}
