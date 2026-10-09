package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.auth.AuthenticatedUser;
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
    public Cliente cadastrar(ClienteRequest request, AuthenticatedUser corretor) {
        return clienteRepository.save(Cliente.cadastrar(request.toDados(),
                corretorRepository.getReferenceById(corretor.id())));
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar(AuthenticatedUser corretor) {
        return clienteRepository.findAllByCorretorIdOrderByNomeAsc(corretor.id());
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(UUID id, AuthenticatedUser corretor) {
        return clienteRepository.findByIdAndCorretorId(id, corretor.id())
                .orElseThrow(() -> new ClienteNaoEncontradoException(id));
    }

    @Transactional
    public Cliente atualizar(
            UUID id,
            ClienteRequest request,
            AuthenticatedUser corretor
    ) {
        Cliente cliente = buscarPorId(id, corretor);
        cliente.atualizarDados(request.toDados());
        return cliente;
    }

    @Transactional
    public void apagar(UUID id, AuthenticatedUser corretor) {
        clienteRepository.delete(buscarPorId(id, corretor));
    }
}
