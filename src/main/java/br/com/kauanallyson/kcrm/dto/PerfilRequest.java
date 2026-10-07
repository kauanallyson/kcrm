package br.com.kauanallyson.kcrm.dto;

import br.com.kauanallyson.kcrm.model.Perfil;
import jakarta.validation.constraints.NotNull;

public record PerfilRequest(
        @NotNull
        Perfil perfil
) {
}
