package br.com.kauanallyson.kcrm.dto.imovel;

import br.com.kauanallyson.kcrm.dto.common.EnderecoResponse;
import br.com.kauanallyson.kcrm.model.imovel.Imovel;
import br.com.kauanallyson.kcrm.model.imovel.Proprietario;
import br.com.kauanallyson.kcrm.model.imovel.Situacao;
import br.com.kauanallyson.kcrm.model.imovel.Tipo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ImovelResponse(
        UUID id,
        Tipo tipo,
        EnderecoResponse endereco,
        BigDecimal precoVenda,
        ProprietarioResponse proprietario,
        BigDecimal area,
        BigDecimal frente,
        BigDecimal fundo,
        Integer quartos,
        Integer suites,
        Integer banheiros,
        Integer vagas,
        Situacao situacao,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
    public static ImovelResponse from(Imovel imovel) {
        return new ImovelResponse(imovel.getId(), imovel.getTipo(), EnderecoResponse.from(imovel.getEndereco()),
                imovel.getPrecoVenda(), ProprietarioResponse.from(imovel.getProprietario()),
                imovel.getArea(), imovel.getFrente(), imovel.getFundo(),
                imovel.getQuartos(), imovel.getSuites(), imovel.getBanheiros(), imovel.getVagas(),
                imovel.getSituacao(), imovel.getCriadoEm(), imovel.getAtualizadoEm());
    }

    public record ProprietarioResponse(String nome, String whatsapp, String email, String cpf) {
        static ProprietarioResponse from(Proprietario proprietario) {
            return new ProprietarioResponse(proprietario.nome(), proprietario.whatsapp().value(),
                    proprietario.email().value(), proprietario.cpf().value());
        }
    }
}
