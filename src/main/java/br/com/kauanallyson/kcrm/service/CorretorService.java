package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.dto.corretor.CadastroCorretorRequest;
import br.com.kauanallyson.kcrm.exception.CorretorJaExisteException;
import br.com.kauanallyson.kcrm.exception.CorretorNaoEncontradoException;
import br.com.kauanallyson.kcrm.model.corretor.Corretor;
import br.com.kauanallyson.kcrm.repository.AdministradorRepository;
import br.com.kauanallyson.kcrm.repository.CorretorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class CorretorService {
    private final CorretorRepository corretorRepository;
    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfirmacaoDeEmailService confirmacaoDeEmail;

    public CorretorService(
            CorretorRepository corretorRepository,
            AdministradorRepository administradorRepository,
            PasswordEncoder passwordEncoder,
            ConfirmacaoDeEmailService confirmacaoDeEmail
    ) {
        this.confirmacaoDeEmail = confirmacaoDeEmail;
        this.corretorRepository = corretorRepository;
        this.administradorRepository = administradorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Com e-mail de conta não confirmada, troca os dados e a senha dela pelos novos, manda um link novo
    // (o anterior deixa de valer) e devolve vazio
    @Transactional
    public Optional<Corretor> cadastrar(CadastroCorretorRequest request) {
        Corretor.Dados dados = request.toDados();
        // O e-mail do Administrador também está tomado: o login não teria como distinguir os dois
        if (administradorRepository.existsByEmail(dados.email())) {
            throw new CorretorJaExisteException();
        }
        Optional<Corretor> existente = corretorRepository.findByEmail(dados.email());
        if (existente.isPresent()) {
            if (existente.get().isEmailConfirmado()) {
                throw new CorretorJaExisteException();
            }
            existente.get().recadastrar(dados, request.senha(), passwordEncoder);
            confirmacaoDeEmail.enviarLink(existente.get());
            return Optional.empty();
        }
        Corretor corretor = corretorRepository.save(Corretor.cadastrar(dados, request.senha(), passwordEncoder));
        confirmacaoDeEmail.enviarLink(corretor);
        return Optional.of(corretor);
    }

    @Transactional(readOnly = true)
    public Corretor buscarPorId(UUID id) {
        return corretorRepository.findById(id).orElseThrow(() -> new CorretorNaoEncontradoException(id));
    }
}
