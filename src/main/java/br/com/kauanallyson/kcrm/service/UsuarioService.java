package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.dto.usuario.CadastroUsuarioRequest;
import br.com.kauanallyson.kcrm.dto.usuario.UsuarioRequest;
import br.com.kauanallyson.kcrm.exception.UsuarioJaExisteException;
import br.com.kauanallyson.kcrm.exception.UsuarioNaoEncontradoException;
import br.com.kauanallyson.kcrm.model.historico.Evento;
import br.com.kauanallyson.kcrm.model.historico.TipoEvento;
import br.com.kauanallyson.kcrm.model.historico.TipoRegistro;
import br.com.kauanallyson.kcrm.model.usuario.Perfil;
import br.com.kauanallyson.kcrm.model.usuario.Usuario;
import br.com.kauanallyson.kcrm.repository.UsuarioRepository;
import br.com.kauanallyson.kcrm.service.historico.HistoricoService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final HistoricoService historicoService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          HistoricoService historicoService,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.historicoService = historicoService;
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
    public Usuario mudarPerfil(UUID id, Perfil perfil, UUID autorId) {
        long adminsAtivos = usuarioRepository.travarAdminsAtivos().size();
        Usuario usuario = buscarPorId(id);
        Perfil anterior = usuario.getPerfil();
        if (usuario.mudarPerfil(perfil, adminsAtivos)) {
            registrar(TipoEvento.MUDANCA_DE_PERFIL, usuario, autorId, null, anterior + " -> " + perfil);
        }
        return usuario;
    }

    @Transactional
    public Usuario desativar(UUID id, String motivo, UUID autorId) {
        long adminsAtivos = usuarioRepository.travarAdminsAtivos().size();
        Usuario usuario = buscarPorId(id);
        usuario.desativar(motivo, adminsAtivos);
        registrar(TipoEvento.DESATIVACAO, usuario, autorId, motivo, null);
        return usuario;
    }

    @Transactional
    public Usuario reativar(UUID id, String motivo, UUID autorId) {
        Usuario usuario = buscarPorId(id);
        usuario.reativar();
        registrar(TipoEvento.REATIVACAO, usuario, autorId, motivo, null);
        return usuario;
    }

    @Transactional(readOnly = true)
    public List<Evento> historico(UUID id) {
        buscarPorId(id);
        return historicoService.eventosDo(TipoRegistro.USUARIO, id);
    }

    private void registrar(TipoEvento tipo, Usuario usuario, UUID autorId, String motivo, String detalhe) {
        historicoService.registrar(tipo, TipoRegistro.USUARIO, usuario.getId(), buscarPorId(autorId), motivo, detalhe);
    }

    @Transactional(readOnly = true)
    public boolean existeAdminAtivo() {
        return usuarioRepository.existsByPerfilAndAtivoTrue(Perfil.ADMIN);
    }
}
