package br.com.kauanallyson.kcrm.config;

import br.com.kauanallyson.kcrm.model.administrador.Administrador;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.repository.AdministradorRepository;
import br.com.kauanallyson.kcrm.repository.CorretorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Cria o único Administrador a partir do ambiente, se ainda não existir. Sem as variáveis, nenhum é criado;
// a senha nunca passa por migration
@Component
public class CriacaoDoAdministrador implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(CriacaoDoAdministrador.class);

    private final AdministradorRepository administradorRepository;
    private final CorretorRepository corretorRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String senha;

    public CriacaoDoAdministrador(
            AdministradorRepository administradorRepository,
            CorretorRepository corretorRepository,
            PasswordEncoder passwordEncoder,
            @Value("${kcrm.administrador.email:}") String email,
            @Value("${kcrm.administrador.senha:}") String senha
    ) {
        this.administradorRepository = administradorRepository;
        this.corretorRepository = corretorRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.senha = senha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() || senha.isBlank()) {
            return;
        }
        if (administradorRepository.count() > 0) {
            return;
        }
        Email valido = new Email(email);
        if (corretorRepository.existsByEmail(valido)) {
            throw new IllegalStateException("ADMINISTRADOR_EMAIL já pertence a um Corretor; use outro e-mail");
        }
        administradorRepository.save(Administrador.criar(valido, senha, passwordEncoder));
        log.info("Administrador criado a partir do ambiente");
    }
}
