package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.auditoria.Auditoria;
import br.com.kauanallyson.kcrm.dto.corretor.CadastroCorretorRequest;
import br.com.kauanallyson.kcrm.exception.CorretorJaExisteException;
import br.com.kauanallyson.kcrm.exception.CorretorNaoEncontradoException;
import br.com.kauanallyson.kcrm.model.corretor.Corretor;
import br.com.kauanallyson.kcrm.repository.CorretorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CorretorService {
    private final CorretorRepository corretorRepository;
    private final PasswordEncoder passwordEncoder;
    private final Auditoria auditoria;

    public CorretorService(CorretorRepository corretorRepository, PasswordEncoder passwordEncoder, Auditoria auditoria) {
        this.corretorRepository = corretorRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoria = auditoria;
    }

    @Transactional
    public Corretor cadastrar(CadastroCorretorRequest request) {
        Corretor.Dados dados = request.toDados();
        if (corretorRepository.existsByEmail(dados.email())) {
            throw new CorretorJaExisteException();
        }
        Corretor corretor = corretorRepository.save(Corretor.cadastrar(dados, request.senha(), passwordEncoder));
        auditoria.registrar("corretor.cadastrado", corretor.getId(), corretor.getId());
        return corretor;
    }

    @Transactional(readOnly = true)
    public Corretor buscarPorId(UUID id) {
        return corretorRepository.findById(id).orElseThrow(() -> new CorretorNaoEncontradoException(id));
    }
}
