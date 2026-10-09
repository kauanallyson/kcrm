package br.com.kauanallyson.kcrm.dto.cliente;

import br.com.kauanallyson.kcrm.dto.common.EnderecoResponse;
import br.com.kauanallyson.kcrm.model.cliente.Cliente;
import br.com.kauanallyson.kcrm.model.cliente.Origem;
import br.com.kauanallyson.kcrm.model.common.Cpf;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ClienteResponse(
        UUID id,
        String nome,
        String whatsapp,
        Origem origem,
        String indicadoPor,
        String cpf,
        String email,
        EnderecoResponse endereco,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
    public static ClienteResponse from(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getNome(), cliente.getWhatsapp().value(),
                cliente.getOrigem(), cliente.getIndicadoPor(),
                cliente.getCpf() == null ? null : cliente.getCpf().value(),
                cliente.getEmail() == null ? null : cliente.getEmail().value(),
                cliente.getEndereco() == null ? null : EnderecoResponse.from(cliente.getEndereco()),
                cliente.getCriadoEm(), cliente.getAtualizadoEm());
    }

    @Override
    public String toString() {
        return "ClienteResponse[id=" + id + ", nome=" + nome + ", whatsapp=" + whatsapp + ", origem=" + origem
                + ", indicadoPor=" + indicadoPor + ", cpf=" + Cpf.mascarar(cpf) + ", email=" + email
                + ", endereco=" + endereco + ", criadoEm=" + criadoEm + ", atualizadoEm=" + atualizadoEm + "]";
    }
}
