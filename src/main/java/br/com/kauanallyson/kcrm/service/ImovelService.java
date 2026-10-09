package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.auth.AuthenticatedUser;
import br.com.kauanallyson.kcrm.dto.imovel.ImovelRequest;
import br.com.kauanallyson.kcrm.exception.ImovelNaoEncontradoException;
import br.com.kauanallyson.kcrm.model.imovel.Imovel;
import br.com.kauanallyson.kcrm.repository.CorretorRepository;
import br.com.kauanallyson.kcrm.repository.ImovelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// Cada Corretor só enxerga os seus Imóveis; o de outro Corretor responde como inexistente
@Service
public class ImovelService {
    private final ImovelRepository imovelRepository;
    private final CorretorRepository corretorRepository;

    public ImovelService(ImovelRepository imovelRepository, CorretorRepository corretorRepository) {
        this.imovelRepository = imovelRepository;
        this.corretorRepository = corretorRepository;
    }

    @Transactional
    public Imovel cadastrar(ImovelRequest request, AuthenticatedUser corretor) {
        return imovelRepository.save(Imovel.cadastrar(request.toDados(),
                corretorRepository.getReferenceById(corretor.id())));
    }

    @Transactional(readOnly = true)
    public List<Imovel> listar(AuthenticatedUser corretor) {
        return imovelRepository.findAllByCorretorIdOrderByCriadoEmDesc(corretor.id());
    }

    @Transactional(readOnly = true)
    public Imovel buscarPorId(UUID id, AuthenticatedUser corretor) {
        return imovelRepository.findByIdAndCorretorId(id, corretor.id())
                .orElseThrow(() -> new ImovelNaoEncontradoException(id));
    }

    @Transactional
    public Imovel atualizar(UUID id, ImovelRequest request, AuthenticatedUser corretor) {
        Imovel imovel = buscarPorId(id, corretor);
        imovel.atualizarDados(request.toDados());
        return imovel;
    }

    @Transactional
    public Imovel marcarVendido(UUID id, AuthenticatedUser corretor) {
        Imovel imovel = buscarPorId(id, corretor);
        imovel.marcarVendido();
        return imovel;
    }

    // Pode apagar a qualquer momento, inclusive Vendido, para desfazer um cadastro ou venda feitos por engano
    @Transactional
    public void apagar(UUID id, AuthenticatedUser corretor) {
        imovelRepository.delete(buscarPorId(id, corretor));
    }
}
