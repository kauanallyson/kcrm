package br.com.kauanallyson.kcrm.dto.usuario;

import br.com.kauanallyson.kcrm.model.usuario.Perfil;
import jakarta.validation.constraints.NotNull;

public record PerfilRequest(
        @NotNull(message = "O perfil não pode ficar em branco")
        Perfil perfil
) {
}
