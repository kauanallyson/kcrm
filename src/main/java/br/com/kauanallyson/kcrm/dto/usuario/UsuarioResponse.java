package br.com.kauanallyson.kcrm.dto.usuario;

import br.com.kauanallyson.kcrm.model.usuario.Perfil;
import br.com.kauanallyson.kcrm.model.usuario.Usuario;

import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nome,
        String cpf,
        String email,
        String telefone,
        String endereco,
        Perfil perfil,
        boolean ativo
) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getCpf().value(),
                usuario.getEmail().value(), usuario.getTelefone().value(), usuario.getEndereco(), usuario.getPerfil(),
                usuario.isAtivo());
    }
}
