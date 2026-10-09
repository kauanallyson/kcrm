package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.dto.conta.ExportacaoDaCarteiraResponse;
import br.com.kauanallyson.kcrm.exception.CorretorNaoEncontradoException;
import br.com.kauanallyson.kcrm.exception.SenhaIncorretaException;
import br.com.kauanallyson.kcrm.model.corretor.Corretor;
import br.com.kauanallyson.kcrm.model.corretor.CorretorId;
import br.com.kauanallyson.kcrm.repository.ClienteRepository;
import br.com.kauanallyson.kcrm.repository.ConfirmacaoDeEmailRepository;
import br.com.kauanallyson.kcrm.repository.CorretorRepository;
import br.com.kauanallyson.kcrm.repository.ImovelRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// O que o Corretor faz com a própria conta (LGPD): exportar a Carteira e encerrar a conta
@Service
public class ContaService {
    private final CorretorRepository corretorRepository;
    private final ClienteRepository clienteRepository;
    private final ImovelRepository imovelRepository;
    private final ConfirmacaoDeEmailRepository confirmacaoRepository;
    private final Carteira carteira;
    private final PasswordEncoder passwordEncoder;

    public ContaService(
            CorretorRepository corretorRepository,
            ClienteRepository clienteRepository,
            ImovelRepository imovelRepository,
            ConfirmacaoDeEmailRepository confirmacaoRepository,
            Carteira carteira,
            PasswordEncoder passwordEncoder
    ) {
        this.corretorRepository = corretorRepository;
        this.clienteRepository = clienteRepository;
        this.imovelRepository = imovelRepository;
        this.confirmacaoRepository = confirmacaoRepository;
        this.carteira = carteira;
        this.passwordEncoder = passwordEncoder;
    }

    // Cópia completa da Carteira, só a do próprio Corretor
    @Transactional(readOnly = true)
    public ExportacaoDaCarteiraResponse exportar(CorretorId corretorId) {
        return ExportacaoDaCarteiraResponse.of(corretor(corretorId), carteira.clientes(corretorId),
                carteira.imoveis(corretorId));
    }

    // Encerramento de Conta: definitivo e imediato, tudo numa transação. Senha errada não apaga nada
    @Transactional
    public void encerrar(CorretorId corretorId, String senha) {
        Corretor corretor = corretor(corretorId);
        if (senha == null || !corretor.getSenhaHash().matches(senha, passwordEncoder)) {
            throw new SenhaIncorretaException();
        }
        clienteRepository.apagarTodosDoCorretor(corretorId.value());
        imovelRepository.apagarTodosDoCorretor(corretorId.value());
        confirmacaoRepository.deleteById(corretorId.value());
        corretorRepository.delete(corretor);
    }

    private Corretor corretor(CorretorId id) {
        return corretorRepository.findById(id.value()).orElseThrow(() -> new CorretorNaoEncontradoException(id.value()));
    }
}
