package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.dto.CadastroUsuarioRequest;
import br.com.kauanallyson.kcrm.dto.UsuarioRequest;
import br.com.kauanallyson.kcrm.exception.UsuarioJaExisteException;
import br.com.kauanallyson.kcrm.exception.UsuarioNaoEncontradoException;
import br.com.kauanallyson.kcrm.model.HistoricoAcesso;
import br.com.kauanallyson.kcrm.model.Perfil;
import br.com.kauanallyson.kcrm.model.Usuario;
import br.com.kauanallyson.kcrm.repository.HistoricoAcessoRepository;
import br.com.kauanallyson.kcrm.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final HistoricoAcessoRepository historicoAcessoRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          HistoricoAcessoRepository historicoAcessoRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.historicoAcessoRepository = historicoAcessoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario cadastrar(CadastroUsuarioRequest request) {
        return cadastrar(request.toDados(), request.perfil(), request.senha());
    }

    @Transactional
    public Usuario cadastrar(Usuario.Dados dados, Perfil perfil, String senha) {
        if (usuarioRepository.existsByEmailOrCpf(dados.email(), dados.cpf())) {
            throw new UsuarioJaExisteException();
        }
        return usuarioRepository.save(Usuario.cadastrar(dados, perfil, senha, passwordEncoder));
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(UUID id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new UsuarioNaoEncontradoException(id));
    }

    @Transactional
    public Usuario atualizar(UUID id, UsuarioRequest request) {
        Usuario usuario = buscarPorId(id);
        Usuario.Dados dados = request.toDados();
        if (usuarioRepository.existsByEmailOrCpfAndIdNot(dados.email(), dados.cpf(), id)) {
            throw new UsuarioJaExisteException();
        }
        usuario.atualizarDados(dados);
        usuario.alterarSenha(request.senha(), passwordEncoder);
        return usuario;
    }

    @Transactional
    public Usuario mudarPerfil(UUID id, Perfil perfil) {
        long adminsAtivos = usuarioRepository.travarAdminsAtivos().size();
        Usuario usuario = buscarPorId(id);
        usuario.mudarPerfil(perfil, adminsAtivos);
        return usuario;
    }

    @Transactional
    public Usuario desativar(UUID id, String motivo, UUID adminId) {
        long adminsAtivos = usuarioRepository.travarAdminsAtivos().size();
        Usuario usuario = buscarPorId(id);
        HistoricoAcesso historico = usuario.desativar(motivo, buscarPorId(adminId), adminsAtivos);
        historicoAcessoRepository.save(historico);
        return usuario;
    }

    @Transactional
    public Usuario reativar(UUID id, String motivo, UUID adminId) {
        Usuario usuario = buscarPorId(id);
        historicoAcessoRepository.save(usuario.reativar(motivo, buscarPorId(adminId)));
        return usuario;
    }

    @Transactional(readOnly = true)
    public List<HistoricoAcesso> historico(UUID id) {
        buscarPorId(id);
        return historicoAcessoRepository.findByUsuarioId(id);
    }

    @Transactional(readOnly = true)
    public boolean existeAdminAtivo() {
        return usuarioRepository.existsByPerfilAndAtivoTrue(Perfil.ADMIN);
    }
}
