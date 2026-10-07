package br.com.kauanallyson.kcrm.dto;

import br.com.kauanallyson.kcrm.model.Perfil;
import br.com.kauanallyson.kcrm.model.Usuario;

import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nome,
        String cpf,
        String email,
        String telefone,
        String endereco,
        Perfil perfil
) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getCpf().value(),
                usuario.getEmail().value(), usuario.getTelefone(), usuario.getEndereco(), usuario.getPerfil());
    }
}
