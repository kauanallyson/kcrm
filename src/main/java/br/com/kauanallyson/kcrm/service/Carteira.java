package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.auditoria.Auditoria;
import br.com.kauanallyson.kcrm.exception.ClienteNaoEncontradoException;
import br.com.kauanallyson.kcrm.exception.ImovelNaoEncontradoException;
import br.com.kauanallyson.kcrm.model.cliente.Cliente;
import br.com.kauanallyson.kcrm.model.corretor.CorretorId;
import br.com.kauanallyson.kcrm.model.imovel.Imovel;
import br.com.kauanallyson.kcrm.repository.ClienteRepository;
import br.com.kauanallyson.kcrm.repository.CorretorRepository;
import br.com.kauanallyson.kcrm.repository.ImovelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// Clientes e Imóveis de um Corretor. Único caminho até eles: o de outro Corretor responde como inexistente
@Service
public class Carteira {
    private final ClienteRepository clienteRepository;
    private final ImovelRepository imovelRepository;
    private final CorretorRepository corretorRepository;
    private final Auditoria auditoria;

    public Carteira(
            ClienteRepository clienteRepository,
            ImovelRepository imovelRepository,
            CorretorRepository corretorRepository,
            Auditoria auditoria
    ) {
        this.auditoria = auditoria;
        this.clienteRepository = clienteRepository;
        this.imovelRepository = imovelRepository;
        this.corretorRepository = corretorRepository;
    }

    @Transactional
    public Cliente cadastrarCliente(CorretorId corretor, Cliente.Dados dados) {
        Cliente cliente = clienteRepository.save(Cliente.cadastrar(dados, corretorRepository.getReferenceById(corretor.value())));
        auditoria.registrar("cliente.criado", corretor.value(), cliente.getId());
        return cliente;
    }

    @Transactional(readOnly = true)
    public Page<Cliente> clientes(CorretorId corretor, Pageable pageable) {
        return clienteRepository.findAllByCorretorId(corretor.value(), pageable);
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
        auditoria.registrar("cliente.alterado", corretor.value(), id);
        return cliente;
    }

    @Transactional
    public void apagarCliente(CorretorId corretor, UUID id) {
        clienteRepository.delete(cliente(corretor, id));
        auditoria.registrar("cliente.apagado", corretor.value(), id);
    }

    @Transactional
    public Imovel cadastrarImovel(CorretorId corretor, Imovel.Dados dados) {
        Imovel imovel = imovelRepository.save(Imovel.cadastrar(dados, corretorRepository.getReferenceById(corretor.value())));
        auditoria.registrar("imovel.criado", corretor.value(), imovel.getId());
        return imovel;
    }

    @Transactional(readOnly = true)
    public Page<Imovel> imoveis(CorretorId corretor, Pageable pageable) {
        return imovelRepository.findAllByCorretorId(corretor.value(), pageable);
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
        auditoria.registrar("imovel.alterado", corretor.value(), id);
        return imovel;
    }

    @Transactional
    public Imovel marcarVendido(CorretorId corretor, UUID id) {
        Imovel imovel = imovel(corretor, id);
        imovel.marcarVendido();
        auditoria.registrar("imovel.vendido", corretor.value(), id);
        return imovel;
    }

    // Pode apagar a qualquer momento, inclusive Vendido, para desfazer um cadastro ou venda feitos por engano
    @Transactional
    public void apagarImovel(CorretorId corretor, UUID id) {
        imovelRepository.delete(imovel(corretor, id));
        auditoria.registrar("imovel.apagado", corretor.value(), id);
    }
}
