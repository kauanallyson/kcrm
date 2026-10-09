package br.com.kauanallyson.kcrm.dto.conta;

import br.com.kauanallyson.kcrm.dto.cliente.ClienteResponse;
import br.com.kauanallyson.kcrm.dto.imovel.ImovelResponse;
import br.com.kauanallyson.kcrm.model.cliente.Cliente;
import br.com.kauanallyson.kcrm.model.corretor.Corretor;
import br.com.kauanallyson.kcrm.model.imovel.Imovel;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// Exportação da Carteira: os dados do Corretor (sem senha) e todos os seus Clientes e Imóveis, com todos os campos
public record ExportacaoDaCarteiraResponse(
        OffsetDateTime exportadaEm,
        CorretorExportado corretor,
        List<ClienteResponse> clientes,
        List<ImovelResponse> imoveis
) {
    public static ExportacaoDaCarteiraResponse of(Corretor corretor, List<Cliente> clientes, List<Imovel> imoveis) {
        return new ExportacaoDaCarteiraResponse(OffsetDateTime.now(), CorretorExportado.from(corretor),
                clientes.stream().map(ClienteResponse::from).toList(),
                imoveis.stream().map(ImovelResponse::from).toList());
    }

    public record CorretorExportado(
            UUID id,
            String nome,
            String email,
            String creci,
            String whatsapp,
            OffsetDateTime cadastradoEm
    ) {
        static CorretorExportado from(Corretor corretor) {
            return new CorretorExportado(corretor.getId(), corretor.getNome(), corretor.getEmail().value(),
                    corretor.getCreci(), corretor.getWhatsapp().value(), corretor.getCriadoEm());
        }
    }
}
