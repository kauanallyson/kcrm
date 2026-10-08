package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.auth.AuthenticatedUser;
import br.com.kauanallyson.kcrm.dto.cliente.ClienteRequest;
import br.com.kauanallyson.kcrm.exception.ClienteNaoEncontradoException;
import br.com.kauanallyson.kcrm.exception.CorretorInvalidoException;
import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;
import br.com.kauanallyson.kcrm.model.cliente.Cliente;
import br.com.kauanallyson.kcrm.model.historico.Evento;
import br.com.kauanallyson.kcrm.model.historico.TipoEvento;
import br.com.kauanallyson.kcrm.model.historico.TipoRegistro;
import br.com.kauanallyson.kcrm.model.usuario.Perfil;
import br.com.kauanallyson.kcrm.model.usuario.Usuario;
import br.com.kauanallyson.kcrm.repository.ClienteRepository;
import br.com.kauanallyson.kcrm.repository.UsuarioRepository;
import br.com.kauanallyson.kcrm.service.historico.HistoricoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// Cada Corretor só enxerga os seus Clientes; o de outro Corretor responde como inexistente
@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistoricoService historicoService;

    public ClienteService(ClienteRepository clienteRepository,
                          UsuarioRepository usuarioRepository,
                          HistoricoService historicoService) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.historicoService = historicoService;
    }

    // O Corretor que cadastra passa a atender o Cliente; o Admin escolhe o Corretor
    @Transactional
    public Cliente cadastrar(ClienteRequest request, AuthenticatedUser autor) {
        Cliente.Dados dados = request.toDados();
        UUID corretorId;
        if (isAdmin(autor)) {
            if (request.corretorId() == null) {
                throw new ValorInvalidoException("corretorId", "Informe o Corretor que vai atender o Cliente");
            }
            corretorId = request.corretorId();
        } else {
            if (request.corretorId() != null) {
                throw new ValorInvalidoException("corretorId",
                        "O Corretor não escolhe o Corretor: o Cliente fica com quem o cadastra");
            }
            corretorId = autor.id();
        }
        return clienteRepository.save(Cliente.cadastrar(dados, corretor(corretorId)));
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar(AuthenticatedUser usuario) {
        if (isAdmin(usuario)) {
            return clienteRepository.findAllByOrderByNomeAsc();
        }
        return clienteRepository.findAllByCorretorIdOrderByNomeAsc(usuario.id());
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(UUID id, AuthenticatedUser usuario) {
        if (isAdmin(usuario)) {
            return clienteRepository.findById(id).orElseThrow(() -> new ClienteNaoEncontradoException(id));
        }
        return clienteRepository.findByIdAndCorretorId(id, usuario.id())
                .orElseThrow(() -> new ClienteNaoEncontradoException(id));
    }

    // A troca de Corretor não é edição: só pela Transferência
    @Transactional
    public Cliente atualizar(UUID id, ClienteRequest request, AuthenticatedUser usuario) {
        if (request.corretorId() != null) {
            throw new ValorInvalidoException("corretorId", "Para trocar o Corretor, faça uma Transferência");
        }
        Cliente cliente = buscarPorId(id, usuario);
        cliente.atualizarDados(request.toDados());
        return cliente;
    }

    @Transactional
    public Cliente transferir(UUID id, UUID corretorId, String motivo, AuthenticatedUser admin) {
        Cliente cliente = buscarPorId(id, admin);
        Usuario anterior = cliente.transferir(corretor(corretorId));
        historicoService.registrar(TipoEvento.TRANSFERENCIA, TipoRegistro.CLIENTE, cliente.getId(),
                usuarioRepository.getReferenceById(admin.id()), motivo,
                anterior.getNome() + " -> " + cliente.getCorretor().getNome());
        return cliente;
    }

    @Transactional(readOnly = true)
    public List<Evento> historico(UUID id, AuthenticatedUser usuario) {
        buscarPorId(id, usuario);
        return historicoService.eventosDo(TipoRegistro.CLIENTE, id);
    }

    // Um id que não aponta para Usuário algum recebe o mesmo erro que um Usuário que não é Corretor ativo
    private Usuario corretor(UUID id) {
        return usuarioRepository.findById(id).orElseThrow(CorretorInvalidoException::new);
    }

    private static boolean isAdmin(AuthenticatedUser usuario) {
        return usuario.perfil() == Perfil.ADMIN;
    }
}
