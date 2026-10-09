package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.auditoria.Auditoria;
import br.com.kauanallyson.kcrm.exception.CorretorNaoEncontradoException;
import br.com.kauanallyson.kcrm.model.corretor.Corretor;
import br.com.kauanallyson.kcrm.repository.CorretorRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// O que o Administrador faz: lista os Corretores e aplica Suspensão. Nunca toca na Carteira
@Service
public class Administracao {
    private final CorretorRepository corretorRepository;
    private final Auditoria auditoria;

    public Administracao(CorretorRepository corretorRepository, Auditoria auditoria) {
        this.corretorRepository = corretorRepository;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<Corretor> corretores() {
        return corretorRepository.findAll(Sort.by("nome"));
    }

    @Transactional
    public void suspender(UUID corretorId) {
        corretor(corretorId).suspender();
        auditoria.registrar("corretor.suspenso", corretorId, corretorId);
    }

    @Transactional
    public void reativar(UUID corretorId) {
        corretor(corretorId).reativar();
        auditoria.registrar("corretor.reativado", corretorId, corretorId);
    }

    private Corretor corretor(UUID id) {
        return corretorRepository.findById(id).orElseThrow(() -> new CorretorNaoEncontradoException(id));
    }
}
