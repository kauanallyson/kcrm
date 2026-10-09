package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.model.corretor.CorretorId;
import br.com.kauanallyson.kcrm.dto.cliente.ClienteRequest;
import br.com.kauanallyson.kcrm.exception.ClienteNaoEncontradoException;
import br.com.kauanallyson.kcrm.model.cliente.Cliente;
import br.com.kauanallyson.kcrm.repository.ClienteRepository;
import br.com.kauanallyson.kcrm.repository.CorretorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// Cada Corretor só enxerga os seus Clientes; o de outro Corretor responde como inexistente
@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final CorretorRepository corretorRepository;

    public ClienteService(ClienteRepository clienteRepository, CorretorRepository corretorRepository) {
        this.clienteRepository = clienteRepository;
        this.corretorRepository = corretorRepository;
    }

    @Transactional
    public Cliente cadastrar(ClienteRequest request, CorretorId corretor) {
        return clienteRepository.save(Cliente.cadastrar(request.toDados(),
                corretorRepository.getReferenceById(corretor.value())));
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar(CorretorId corretor) {
        return clienteRepository.findAllByCorretorIdOrderByNomeAsc(corretor.value());
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(UUID id, CorretorId corretor) {
        return clienteRepository.findByIdAndCorretorId(id, corretor.value())
                .orElseThrow(() -> new ClienteNaoEncontradoException(id));
    }

    @Transactional
    public Cliente atualizar(
            UUID id,
            ClienteRequest request,
            CorretorId corretor
    ) {
        Cliente cliente = buscarPorId(id, corretor);
        cliente.atualizarDados(request.toDados());
        return cliente;
    }

    @Transactional
    public void apagar(UUID id, CorretorId corretor) {
        clienteRepository.delete(buscarPorId(id, corretor));
    }
}
