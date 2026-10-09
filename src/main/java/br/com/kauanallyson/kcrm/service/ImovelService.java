package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.model.corretor.CorretorId;
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
    public Imovel cadastrar(ImovelRequest request, CorretorId corretor) {
        return imovelRepository.save(Imovel.cadastrar(request.toDados(),
                corretorRepository.getReferenceById(corretor.value())));
    }

    @Transactional(readOnly = true)
    public List<Imovel> listar(CorretorId corretor) {
        return imovelRepository.findAllByCorretorIdOrderByCriadoEmDesc(corretor.value());
    }

    @Transactional(readOnly = true)
    public Imovel buscarPorId(UUID id, CorretorId corretor) {
        return imovelRepository.findByIdAndCorretorId(id, corretor.value())
                .orElseThrow(() -> new ImovelNaoEncontradoException(id));
    }

    @Transactional
    public Imovel atualizar(
            UUID id,
            ImovelRequest request,
            CorretorId corretor
    ) {
        Imovel imovel = buscarPorId(id, corretor);
        imovel.atualizarDados(request.toDados());
        return imovel;
    }

    @Transactional
    public Imovel marcarVendido(UUID id, CorretorId corretor) {
        Imovel imovel = buscarPorId(id, corretor);
        imovel.marcarVendido();
        return imovel;
    }

    // Pode apagar a qualquer momento, inclusive Vendido, para desfazer um cadastro ou venda feitos por engano
    @Transactional
    public void apagar(UUID id, CorretorId corretor) {
        imovelRepository.delete(buscarPorId(id, corretor));
    }
}
