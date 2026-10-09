package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.exception.ClienteNaoEncontradoException;
import br.com.kauanallyson.kcrm.exception.ImovelNaoEncontradoException;
import br.com.kauanallyson.kcrm.model.cliente.Cliente;
import br.com.kauanallyson.kcrm.model.corretor.CorretorId;
import br.com.kauanallyson.kcrm.model.imovel.Imovel;
import br.com.kauanallyson.kcrm.repository.ClienteRepository;
import br.com.kauanallyson.kcrm.repository.CorretorRepository;
import br.com.kauanallyson.kcrm.repository.ImovelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// Clientes e Imóveis de um Corretor. Único caminho até eles: o de outro Corretor responde como inexistente
@Service
public class Carteira {
    private final ClienteRepository clienteRepository;
    private final ImovelRepository imovelRepository;
    private final CorretorRepository corretorRepository;

    public Carteira(
            ClienteRepository clienteRepository,
            ImovelRepository imovelRepository,
            CorretorRepository corretorRepository
    ) {
        this.clienteRepository = clienteRepository;
        this.imovelRepository = imovelRepository;
        this.corretorRepository = corretorRepository;
    }

    @Transactional
    public Cliente cadastrarCliente(CorretorId corretor, Cliente.Dados dados) {
        return clienteRepository.save(Cliente.cadastrar(dados, corretorRepository.getReferenceById(corretor.value())));
    }

    @Transactional(readOnly = true)
    public List<Cliente> clientes(CorretorId corretor) {
        return clienteRepository.findAllByCorretorIdOrderByNomeAsc(corretor.value());
    }

    @Transactional(readOnly = true)
    public Cliente cliente(CorretorId corretor, UUID id) {
        return clienteRepository.findByIdAndCorretorId(id, corretor.value())
                .orElseThrow(() -> new ClienteNaoEncontradoException(id));
    }

    @Transactional
    public Cliente atualizarCliente(
            CorretorId corretor,
            UUID id,
            Cliente.Dados dados
    ) {
        Cliente cliente = cliente(corretor, id);
        cliente.atualizarDados(dados);
        return cliente;
    }

    @Transactional
    public void apagarCliente(CorretorId corretor, UUID id) {
        clienteRepository.delete(cliente(corretor, id));
    }

    @Transactional
    public Imovel cadastrarImovel(CorretorId corretor, Imovel.Dados dados) {
        return imovelRepository.save(Imovel.cadastrar(dados, corretorRepository.getReferenceById(corretor.value())));
    }

    @Transactional(readOnly = true)
    public List<Imovel> imoveis(CorretorId corretor) {
        return imovelRepository.findAllByCorretorIdOrderByCriadoEmDesc(corretor.value());
    }

    @Transactional(readOnly = true)
    public Imovel imovel(CorretorId corretor, UUID id) {
        return imovelRepository.findByIdAndCorretorId(id, corretor.value())
                .orElseThrow(() -> new ImovelNaoEncontradoException(id));
    }

    @Transactional
    public Imovel atualizarImovel(
            CorretorId corretor,
            UUID id,
            Imovel.Dados dados
    ) {
        Imovel imovel = imovel(corretor, id);
        imovel.atualizarDados(dados);
        return imovel;
    }

    @Transactional
    public Imovel marcarVendido(CorretorId corretor, UUID id) {
        Imovel imovel = imovel(corretor, id);
        imovel.marcarVendido();
        return imovel;
    }

    // Pode apagar a qualquer momento, inclusive Vendido, para desfazer um cadastro ou venda feitos por engano
    @Transactional
    public void apagarImovel(CorretorId corretor, UUID id) {
        imovelRepository.delete(imovel(corretor, id));
    }
}
