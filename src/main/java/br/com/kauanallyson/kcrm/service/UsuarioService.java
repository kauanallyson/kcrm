package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.dto.UsuarioRequest;
import br.com.kauanallyson.kcrm.exception.UsuarioJaExisteException;
import br.com.kauanallyson.kcrm.exception.UsuarioNaoEncontradoException;
import br.com.kauanallyson.kcrm.model.Usuario;
import br.com.kauanallyson.kcrm.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario cadastrar(UsuarioRequest request) {
        Usuario.Dados dados = request.toDados();
        if (usuarioRepository.existsByEmailOrCpf(dados.email(), dados.cpf())) {
            throw new UsuarioJaExisteException();
        }
        return usuarioRepository.save(Usuario.cadastrar(dados, request.senha(), passwordEncoder));
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
    public void excluir(UUID id) {
        if (!usuarioRepository.existsById(id)) {
            throw new UsuarioNaoEncontradoException(id);
        }
        usuarioRepository.deleteById(id);
    }
}
