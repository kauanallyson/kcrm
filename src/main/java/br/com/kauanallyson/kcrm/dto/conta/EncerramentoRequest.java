package br.com.kauanallyson.kcrm.dto.conta;

import jakarta.validation.constraints.NotBlank;

// O Corretor confirma a própria senha para encerrar a conta
public record EncerramentoRequest(
        @NotBlank(message = "A senha não pode ficar em branco")
        String senha
) {
    @Override
    public String toString() {
        return "EncerramentoRequest[senha=****]";
    }
}
